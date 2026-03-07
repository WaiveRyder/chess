package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import service.GameService;
import service.UserService;
import service.requests.*;
import service.responses.AuthResponse;
import service.responses.CreateGameResponse;
import service.responses.GenericResponse;
import service.responses.ListGamesResponse;

import java.util.Objects;

public class Server {
    private final Gson serializer;
    private final UserService userService;
    private final GameService gameService;

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        // Register your endpoints and exception handlers here.
        DatabaseManager databaseManager = new DatabaseManager();
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
        //Register a user
        javalin.post("/user", new Handler() {
            public void handle(@NotNull Context context) {
                RegisterRequest request = serializer.fromJson(context.body(), RegisterRequest.class);
                context.contentType("application/json");
                handleRegister(context, request);
            }
        });
        //Login a user
        javalin.post("/session", new Handler() {
            public void handle(@NotNull Context context) {
                LoginRequest request = serializer.fromJson(context.body(), LoginRequest.class);
                context.contentType("application/json");
                handleLogin(context, request);
            }
        });
        //Logout a user
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
        //List all games
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
        //Create new game
        javalin.post("/game", new Handler() {
            public void handle(@NotNull Context context) {
                CreateGameRequest body = serializer.fromJson(context.body(), CreateGameRequest.class);
                CreateGameRequest request = new CreateGameRequest(context.header("Authorization"), body.gameName());
                context.contentType("application/json");

                handleCreateGame(context, request);
            }
        });
        //Join a game
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
        //Clear
        javalin.delete("/db", new Handler() {
            public void handle(@NotNull Context context) {
                handleClear(context);
            }
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
            context.result(serializer.toJson(new GenericResponse("Error: No Null Elements Allowed")));
            context.status(400);
        } else {

            GenericResponse response = gameService.joinGame(request);
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
}
