package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import service.GameService;
import service.UserService;
import service.requests.*;
import service.responses.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import static websocket.messages.ServerMessage.ServerMessageType.*;

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
            public void handle(@NotNull Context context) {handleCreateGame(context);}
        });
        javalin.put("/game", new Handler() {
            public void handle(@NotNull Context context) {handleJoinGame(context);}
        });
        javalin.put("/game/move", new Handler() {
            public void handle(@NotNull Context context) {handleMakeMove(context);}
        });
        javalin.put("/game/leave", new Handler() {
            public void handle(@NotNull Context context) {handleLeaveGame(context);}
        });
        javalin.put("/game/resign", new Handler() {
            public void handle(@NotNull Context context) {handleResign(context);}
        });
        javalin.get("/observe", new Handler() {
            public void handle(@NotNull Context context) {handleObserveGame(context, false);}
        });
        javalin.delete("/observe", new Handler() {
            public void handle(@NotNull Context context) {handleObserveGame(context, true);}
        });
        javalin.delete("/db", new Handler() {
            public void handle(@NotNull Context context) {handleClear(context);}
        });
        javalin.ws("/ws", ws -> {
            ws.onConnect(WsContext::enableAutomaticPings);
            ws.onMessage(this::handleWebsocketMessage);
            ws.onClose(this::handleWSClose);
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

    private void handleCreateGame(Context context) {
        CreateGameRequest body = gson.fromJson(context.body(), CreateGameRequest.class);
        CreateGameRequest request = new CreateGameRequest(context.header("Authorization"), body.gameName());
        context.contentType("application/json");
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

    private void handleJoinGame(Context context) {
        JoinGameRequest body = gson.fromJson(context.body(), JoinGameRequest.class);
        JoinGameRequest request = new JoinGameRequest(
                context.header("Authorization"),
                body.playerColor(),
                body.gameID()
        );

        context.contentType("application/json");
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

    private void handleLeaveGame(Context context) {
        JoinGameRequest body = gson.fromJson(context.body(), JoinGameRequest.class);
        JoinGameRequest request = new JoinGameRequest(
                context.header("Authorization"),
                body.playerColor(),
                body.gameID()
        );
        context.contentType("application/json");
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

    private void handleObserveGame(Context context, boolean leave) {
        ObserveGameRequest request = new ObserveGameRequest(Integer.parseInt(Objects.requireNonNull(context.header("gameID"))),
                context.header("Authorization"), leave);
        context.contentType("application/json");
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

        for (Vector<Session> sessions : wsSessions.values()) {
            for (Session s : sessions) {
                if (s.isOpen()) {
                    s.close();
                }
            }
        }

        wsSessions.clear();

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

    private void handleResign(Context context) {
        ResignGameRequest request = gson.fromJson(context.body(), ResignGameRequest.class);
        request = new ResignGameRequest(context.header("Authorization"), request.gameID());
        context.contentType("application/json");
        if (request.gameID() == null || request.authToken() == null) {
            context.result(gson.toJson(new GenericResponse("Error: No Null Elements Allowed")));
            context.status(400);
        } else {
            GenericResponse response = gameService.resignGame(request);
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

    private void handleWebsocketMessage(WsMessageContext ctx) {
        UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> handleWSConnect(command, ctx.session);
            case LEAVE -> handleWSLeave(command, ctx.session);
            case MAKE_MOVE -> handleWSMove(command, ctx.session);
            case RESIGN -> handleWSResign(command, ctx.session);
        }
    }

    private void handleWSResign(UserGameCommand command, Session session) {
        try {
            String username = authDAO.getAuthData(command.getAuthToken()).username();
            int gameID = command.getGameID();

            ServerMessage msg = new ServerMessage(NOTIFICATION, username+" resigned the game", null);
            Vector<Session> sessions = wsSessions.get(gameID);
            sendWSMessage(sessions, null, msg);

        } catch (DataAccessException e) {
            //Implement
        }
    }

    private void handleWSLeave(UserGameCommand command, Session session) {
        try {
            String username = authDAO.getAuthData(command.getAuthToken()).username();
            int gameID = command.getGameID();

            Vector<Session> sessions = wsSessions.get(gameID);
            if (sessions != null) {
                ServerMessage msg = new ServerMessage(
                        ServerMessage.ServerMessageType.NOTIFICATION,
                        username + " left the game" + command.getMessage(), null);
                sendWSMessage(sessions, session, msg);
            }
        } catch (DataAccessException e) {
            //Implement
        }
    }

    private void handleWSConnect(UserGameCommand command, Session session) {
        try {
            String username = authDAO.getAuthData(command.getAuthToken()).username();
            int gameID = command.getGameID();
            ChessGame game = gameDAO.getGame(gameID).game();

            Vector<Session> sessions = wsSessions.putIfAbsent(gameID, new Vector<>());
            if (sessions != null) {
                sessions.add(session);
                ServerMessage msg = new ServerMessage(
                        ServerMessage.ServerMessageType.NOTIFICATION,
                        username + " connected to the game" + command.getMessage(), null);
                sendWSMessage(sessions, session, msg);

            } else {
                wsSessions.get(gameID).add(session);
            }
            try {
                session.getRemote().sendString(gson.toJson(new ServerMessage(LOAD_GAME, null, game)));
            } catch (Exception e) {
                //Implement
            }
        } catch (DataAccessException e) {
            try {
                ServerMessage msg = new ServerMessage(ERROR, e.getMessage());
                session.getRemote().sendString(gson.toJson(msg));
            } catch (Exception ex) {
                //Implement
            }

        }
    }

    private void handleWSMove(UserGameCommand command, Session session) {
        Vector<Session> sessions = wsSessions.get(command.getGameID());
        try {
            String username = authDAO.getAuthData(command.getAuthToken()).username();
            GameData gameData = gameDAO.getGame(command.getGameID());
            ChessGame game = gameData.game();

            MakeMoveRequest r = new MakeMoveRequest(
                    command.getAuthToken(), command.getGameID(), command.getMove());
            ReturnGameResponse response = gameService.makeMove(r);
            if (response.message().isEmpty()) {
                game = gameDAO.getGame(command.getGameID()).game();
            } else {
                ServerMessage msg = new ServerMessage(ERROR, response.message());
                session.getRemote().sendString(gson.toJson(msg));
                return;
            }

            ServerMessage msg = new ServerMessage(LOAD_GAME, null, game);
            ServerMessage msgNotif = new ServerMessage(NOTIFICATION, username
                    + " made move " + command.getMessage(), null);
            sendWSMessage(sessions, null, msg);
            sendWSMessage(sessions, session, msgNotif);

            ServerMessage msg2 = null;
            if (game.isInStalemate(ChessGame.TeamColor.WHITE)) {
                msg2 = new ServerMessage(NOTIFICATION, gameData.whiteUsername() + " is in stalemate", null);
            } else if (game.isInStalemate(ChessGame.TeamColor.BLACK)) {
                msg2 = new ServerMessage(NOTIFICATION, gameData.blackUsername() + " is in stalemate", null);
            } else if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                msg2 = new ServerMessage(NOTIFICATION, gameData.whiteUsername() + " is in checkmate", null);
            } else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                msg2 = new ServerMessage(NOTIFICATION, gameData.blackUsername() + " is in checkmate", null);
            } else if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
                msg2 = new ServerMessage(NOTIFICATION, gameData.whiteUsername() + " is in check", null);
            } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
                msg2 = new ServerMessage(NOTIFICATION, gameData.blackUsername() + " is in check", null);
            }

            if (msg2 != null) {
                sendWSMessage(sessions, null, msg2);
            }
        } catch (IOException | DataAccessException e) {
            try {
                if (e instanceof DataAccessException) {
                    session.getRemote().sendString(gson.toJson(new ServerMessage(ERROR, e.getMessage())));
                }
            } catch (Exception et) {
                //Implement
            }
        }
    }

    private void sendWSMessage(Vector<Session> sessions, Session session, ServerMessage msg) {
        Iterator<Session> sessionIterator = sessions.iterator();
        while(sessionIterator.hasNext()) {
            Session s = sessionIterator.next();
            if (!s.equals(session) && s.isOpen()) {
                try {
                    s.getRemote().sendString(gson.toJson(msg));
                } catch (Exception e) {
                    //Implement
                }

            } else if (!s.isOpen()) {
                sessionIterator.remove();
            }
        }
    }

    private void handleMakeMove(Context context) {
        UserGameCommand request = gson.fromJson(context.body(), UserGameCommand.class);
        context.contentType("application/json");
        if (request.getAuthToken() == null || request.getGameID() == null || request.getMessage() == null) {
            context.result(gson.toJson(new GenericResponse("Error: No Null Elements Allowed")));
            context.status(400);
        } else {
            MakeMoveRequest requestMove = new MakeMoveRequest(
                    request.getAuthToken(), request.getGameID(), request.getMove());
            ReturnGameResponse response = gameService.makeMove(requestMove);
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

    public void handleWSClose(WsCloseContext ctx) {
        Session s = ctx.session;
        Iterator <Vector<Session>> sessionIterator = wsSessions.values().iterator();
        Iterator <Session> innerIterator;
        while (sessionIterator.hasNext()) {
            innerIterator = sessionIterator.next().iterator();
            while (innerIterator.hasNext()) {
                Session singleSession = innerIterator.next();
                if (singleSession.equals(s)) {
                    innerIterator.remove();
                    s.close();
                    return;
                }
            }
        }
    }
}
