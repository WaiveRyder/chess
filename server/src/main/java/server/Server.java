package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import service.GameService;
import service.UserService;
import service.requests.*;
import service.responses.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.Objects;
import java.util.Vector;

public class Server {
    private final Gson gson;

    private final AuthDAO authDAO;
    private final UserService userService;
    private final GameDAO gameDAO;
    private final GameService gameService;

    private final Javalin javalin;

    private static Map<Integer, Vector<Session>> wsSessions;

    public Server() {
        wsSessions = new java.util.concurrent.ConcurrentHashMap<>();
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.initTables();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        UserDAO userDAO = new UserDAO();
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
        gameService = new GameService(authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);
        gson = new Gson();
        javalin.post("/user", new Handler() {
            public void handle(@NotNull Context context) {
                RegisterRequest request = gson.fromJson(context.body(), RegisterRequest.class);
                context.contentType("application/json");
                handleRegister(context, request);
            }
        });
        javalin.post("/session", new Handler() {
            public void handle(@NotNull Context context) {
                LoginRequest request = gson.fromJson(context.body(), LoginRequest.class);
                context.contentType("application/json");
                handleLogin(context, request);
            }
        });
        javalin.delete("/session", new Handler() {
            public void handle(@NotNull Context context) {
                AuthRequest request = new AuthRequest(context.header("Authorization"));
                GenericResponse response = userService.logoutUser(request);
                context.contentType("application/json");
                context.result(gson.toJson(response));
                if(Objects.equals(response.message(), "")) {
                    context.status(200);
                } else if (response.message().contains("connect")){
                    context.status(500);
                } else {
                    context.status(401);
                }
            }
        });
        javalin.get("/game", new Handler() {
            public void handle(@NotNull Context context) {
                AuthRequest request = new AuthRequest(context.header("Authorization"));
                ListGamesResponse response = gameService.listGames(request);
                context.contentType("application/json");
                context.result(gson.toJson(response));
                if(Objects.equals(response.message(), "")) {
                    context.status(200);
                } else if (response.message().contains("connect")) {
                    context.status(500);
                } else {
                    context.status(401);
                }
            }
        });
        javalin.post("/game", new Handler() {
            public void handle(@NotNull Context context) {
                CreateGameRequest body = gson.fromJson(context.body(), CreateGameRequest.class);
                CreateGameRequest request = new CreateGameRequest(context.header("Authorization"), body.gameName());
                context.contentType("application/json");
                handleCreateGame(context, request);
            }
        });
        javalin.put("/game/join", new Handler() {
            public void handle(@NotNull Context context) {
                JoinGameRequest body = gson.fromJson(context.body(), JoinGameRequest.class);
                JoinGameRequest request = new JoinGameRequest(
                        context.header("Authorization"),
                        body.playerColor(),
                        body.gameID()
                );
                context.contentType("application/json");
                handleJoinGame(context, request);
            }
        });
        javalin.put("/game/leave", new Handler() {
            public void handle(@NotNull Context context) {
                JoinGameRequest body = gson.fromJson(context.body(), JoinGameRequest.class);
                JoinGameRequest request = new JoinGameRequest(
                        context.header("Authorization"),
                        body.playerColor(),
                        body.gameID()
                );
                context.contentType("application/json");
                handleLeaveGame(context, request);
            }
        });
        javalin.get("/observe", new Handler() {
            public void handle(@NotNull Context context) {
                ObserveGameRequest request = new ObserveGameRequest(Integer.parseInt(Objects.requireNonNull(context.header("gameID"))),
                        context.header("Authorization"), false);
                context.contentType("application/json");
                handleObserveGame(context, request);
            }
        });
        javalin.delete("/observe", new Handler() {
            public void handle(@NotNull Context context) {
                ObserveGameRequest request = new ObserveGameRequest(Integer.parseInt(Objects.requireNonNull(context.header("gameID"))),
                        context.header("Authorization"), true);
                context.contentType("application/json");
                handleObserveGame(context, request);
            }
        });
        javalin.delete("/db", new Handler() {
            public void handle(@NotNull Context context) {
                handleClear(context);
            }});
        javalin.ws("/ws", ws -> {
            ws.onConnect(WsContext::enableAutomaticPings);
            ws.onMessage(this::handleWebsocketMessage);
            ws.onClose(WsContext::closeSession);
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void handleRegister(Context context, RegisterRequest request) {
        if(request.username() == null || request.password() == null || request.email() == null) {
            context.result(gson.toJson(new AuthResponse(
                    null,
                    null,
                    "Error: No Null Elements Allowed"
            )));
            context.status(400);
        } else {

            AuthResponse response = userService.registerUser(request);

            context.result(gson.toJson(response));
            if (Objects.equals(response.message(), "")) {
                context.status(200);
            } else if (response.message().contains("connect")) {
                context.status(500);
            } else {
                context.status(403);
            }
        }
    }

    private void handleLogin(Context context, LoginRequest request) {
        if (request.username() == null || request.password() == null) {
            context.result(gson.toJson(new AuthResponse(
                    null,
                    null,
                    "Error: No Null Elements Allowed")));
            context.status(400);
        } else {

            AuthResponse response = userService.loginUser(request);


            context.result(gson.toJson(response));
            if (Objects.equals(response.message(), "")) {
                context.status(200);
            } else if (response.message().contains("connect")) {
                context.status(500);
            } else {
                context.status(401);
            }
        }
    }

    private void handleCreateGame(Context context, CreateGameRequest request) {
        if (request.authToken() == null || request.gameName() == null) {
            context.result(gson.toJson(new CreateGameResponse(
                    null,
                    "Error: No Null Elements Allowed"
            )));
            context.status(400);
        } else {
            CreateGameResponse response = gameService.createGame(request);
            context.result(gson.toJson(response));
            if (Objects.equals(response.message(), "")) {
                context.status(200);
            } else if (response.message().contains("connect")){
                context.status(500);
            } else {
                context.status(401);
            }
        }
    }

    private void handleJoinGame(Context context, JoinGameRequest request) {
        if (request.gameID() == null || request.playerColor() == null || request.authToken() == null) {
            context.result(gson.toJson(new ReturnGameResponse(null,"Error: No Null Elements Allowed")));
            context.status(400);
        } else {

            ReturnGameResponse response = gameService.joinGame(request);
            context.result(gson.toJson(response));
            if (Objects.equals(response.message(), "")) {
                context.status(200);
            } else if (response.message().contains("taken")) {
                context.status(403);
            } else if (response.message().contains("token")) {
                context.status(401);
            } else if (response.message().contains("connect")) {
                context.status(500);
            } else {
                context.status(400);
            }
        }
    }

    private void handleLeaveGame(Context context, JoinGameRequest request) {
        if (request.gameID() == null || request.playerColor() == null || request.authToken() == null) {
            context.result(gson.toJson(new GenericResponse("Error: No Null Elements Allowed")));
            context.status(400);
        } else {

            GenericResponse response = gameService.leaveGame(request);
            context.result(gson.toJson(response));
            if (Objects.equals(response.message(), "")) {
                context.status(200);
            } else if (response.message().contains("taken")) {
                context.status(403);
            } else if (response.message().contains("token")) {
                context.status(401);
            } else if (response.message().contains("connect")) {
                context.status(500);
            } else {
                context.status(400);
            }
        }
    }

    private void handleObserveGame(Context context, ObserveGameRequest request) {
        if (request.gameID() == null || request.token() == null) {
            context.result(gson.toJson(new ReturnGameResponse(
                    null,
                    "Error: No Null Elements Allowed"
            )));
            context.status(400);
        } else {
            ReturnGameResponse response = gameService.observeGame(request);
            context.result(gson.toJson(response));
            if (Objects.equals(response.message(), "")) {
                context.status(200);
            } else if (response.message().contains("token")) {
                context.status(401);
            } else if (response.message().contains("connect")) {
                context.status(500);
            } else {
                context.status(400);
            }
        }
    }

    private void handleClear(Context context) {
        GenericResponse gameClear = gameService.clear();
        GenericResponse userClear = userService.clear();

        context.contentType("application/jason");

        if (!Objects.equals(userClear.message(), "")) {
            context.status(500);
            context.result(gson.toJson(userClear));
        } else if (!Objects.equals(gameClear.message(), "")) {
            context.status(500);
            context.result(gson.toJson(gameClear));
        } else {
            context.status(200);
            context.result(gson.toJson(new GenericResponse("")));
        }
    }

    private void handleWebsocketMessage(WsMessageContext ctx) {
        UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> handleWSConnect(command, ctx.session);
        }
    }

    private void handleWSConnect(UserGameCommand command, Session session) {
        try {
            String username = authDAO.getAuthData(command.getAuthToken()).username();
            int gameID = command.getGameID();
            Vector<Session> sessions = wsSessions.putIfAbsent(gameID, new Vector<>());
            if (sessions != null) {
                sessions.add(session);
                ServerMessage msg = new ServerMessage(
                        ServerMessage.ServerMessageType.NOTIFICATION,
                        username + " connected to the game.");
                sendWSMessage(sessions, session, msg);
            }
        } catch (DataAccessException e) {
            //Implement
        }
    }

    private void sendWSMessage(Vector<Session> sessions, Session session, ServerMessage msg) {
        for (Session s: sessions) {
            if (!s.equals(session) && s.isOpen()) {
                try {
                    s.getRemote().sendString(gson.toJson(msg));
                } catch (Exception e) {
                    //Implement
                }

            } else if (!s.isOpen()) {
                sessions.remove(s);
            }
        }
    }
}
