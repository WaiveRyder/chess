package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import service.GameService;
import service.UserService;
import service.requests.AuthRequest;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;
import service.responses.AuthResponse;
import service.responses.GenericResponse;

import java.util.Objects;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        UserDAO userDAO = new UserDAO();
        AuthDAO authDAO = new AuthDAO();
        GameDAO gameDAO = new GameDAO();

        GameService gameService = new GameService(authDAO, gameDAO);
        UserService userService = new UserService(userDAO, authDAO);

        var serializer = new Gson();

        javalin.post("/user", new Handler() {
            public void handle(@NotNull Context context) {
                RegisterRequest request = serializer.fromJson(context.body(), RegisterRequest.class);
                AuthResponse response = userService.registerUser(request);

                context.contentType("application/json");
                context.result(serializer.toJson(response));
                if(Objects.equals(response.message(), "")) {
                    context.status(200);
                } else {
                    context.status(400);
                }
            }
        });

        javalin.post("/session", new Handler() {
            public void handle(@NotNull Context context) {
                LoginRequest request = serializer.fromJson(context.body(), LoginRequest.class);
                AuthResponse response = userService.loginUser(request);

                context.contentType("application/json");
                context.result(serializer.toJson(response));
                if(Objects.equals(response.message(), "")) {
                    context.status(200);
                } else {
                    context.status(400);
                }
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
}
