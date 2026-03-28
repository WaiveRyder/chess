package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.websocket.WsMessageContext;
import org.jetbrains.annotations.NotNull;
import service.GameService;
import service.UserService;
import service.requests.*;
import service.responses.*;

import java.util.Objects;

public class Server {
    private final Gson serializer;
    private final UserService userService;
    private final GameService gameService;

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.initTables();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        UserDAO userDAO = new UserDAO();
        AuthDAO authDAO = new AuthDAO();
        GameDAO gameDAO = new GameDAO();
        gameService = new GameService(authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);
        serializer = new Gson();
        javalin.post("/user", new Handler() {
            public void handle(@NotNull Context context) {
                RegisterRequest request = serializer.fromJson(context.body(), RegisterRequest.class);
                context.contentType("application/json");
                handleRegister(context, request);
            }
        });
        javalin.post("/session", new Handler() {
            public void handle(@NotNull Context context) {
                LoginRequest request = serializer.fromJson(context.body(), LoginRequest.class);
                context.contentType("application/json");
                handleLogin(context, request);
            }
        });
        javalin.delete("/session", new Handler() {
            public void handle(@NotNull Context context) {
                AuthRequest request = new AuthRequest(context.header("Authorization"));
                GenericResponse response = userService.logoutUser(request);
                context.contentType("application/json");
                context.result(serializer.toJson(response));
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
                context.result(serializer.toJson(response));
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
                CreateGameRequest body = serializer.fromJson(context.body(), CreateGameRequest.class);
                CreateGameRequest request = new CreateGameRequest(context.header("Authorization"), body.gameName());
                context.contentType("application/json");
                handleCreateGame(context, request);
            }
        });
        javalin.put("/game", new Handler() {
            public void handle(@NotNull Context context) {
                JoinGameRequest body = serializer.fromJson(context.body(), JoinGameRequest.class);
                JoinGameRequest request = new JoinGameRequest(
                        context.header("Authorization"),
                        body.playerColor(),
                        body.gameID()
                );
                context.contentType("application/json");
                handleJoinGame(context, request);
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
            ws.onConnect(ctx -> {
                ctx.enableAutomaticPings();
                System.out.println("WebSocket Connected: " + ctx);
            });
            ws.onMessage(this::handleWebsocket);
            ws.onClose(ctx -> System.out.println("WebSocket Closed: " + ctx));
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
            context.result(serializer.toJson(new AuthResponse(
                    null,
                    null,
                    "Error: No Null Elements Allowed"
            )));
            context.status(400);
        } else {

            AuthResponse response = userService.registerUser(request);

            context.result(serializer.toJson(response));
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
            context.result(serializer.toJson(new AuthResponse(
                    null,
                    null,
                    "Error: No Null Elements Allowed")));
            context.status(400);
        } else {

            AuthResponse response = userService.loginUser(request);


            context.result(serializer.toJson(response));
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
            context.result(serializer.toJson(new CreateGameResponse(
                    null,
                    "Error: No Null Elements Allowed"
            )));
            context.status(400);
        } else {
            CreateGameResponse response = gameService.createGame(request);
            context.result(serializer.toJson(response));
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
            context.result(serializer.toJson(new ReturnGameResponse(null,"Error: No Null Elements Allowed")));
            context.status(400);
        } else {

            ReturnGameResponse response = gameService.joinGame(request);
            context.result(serializer.toJson(response));
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
            context.result(serializer.toJson(new ReturnGameResponse(
                    null,
                    "Error: No Null Elements Allowed"
            )));
            context.status(400);
        } else {
            ReturnGameResponse response = gameService.observeGame(request);
            context.result(serializer.toJson(response));
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
            context.result(serializer.toJson(userClear));
        } else if (!Objects.equals(gameClear.message(), "")) {
            context.status(500);
            context.result(serializer.toJson(gameClear));
        } else {
            context.status(200);
            context.result(serializer.toJson(new GenericResponse("")));
        }
    }

    private void handleWebsocket(WsMessageContext ctx) {

    }
}
