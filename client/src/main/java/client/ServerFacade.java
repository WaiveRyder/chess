package client;

import Responses.Auth;
import Responses.ListGames;
import Responses.Message;
import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import model.GameData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;

public class ServerFacade {
    int port;
    HttpClient client;
    Gson gson;
    String authToken;
    State state;

    public ServerFacade(int port, State state) {
        this.port = port;
        client = HttpClient.newHttpClient();
        gson = new Gson();
        this.state = state;
    }

    public void request(String... args) {
        if (args.length == 0) {
            ClientDraw.printError("No command provided");
        } else {
            switch (args[0].toLowerCase()) {
                case "login" -> loginHandler(args);
                case "register" -> registerHandler(args);
                case "list" -> listHandler(args);
                case "join" -> joinHandler(args);
                case "create" -> createHandler(args);
                case "observe" -> observeHandler(args);
                case "logout" -> logoutHandler(args);
                case "leave" -> leaveHandler(args);
                default -> ClientDraw.printError("Unknown command: " + args[0]);
            }
        }
    }

    private void loginHandler(String... args) {
        if (args.length != 3) {
            ClientDraw.printError("Usage: login <username> <password>");
        } else {
            String username = args[1];
            String password = args[2];
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/session"))
                    .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}"))
                    .header("Content-Type", "application/json")
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    Auth authResponse = gson.fromJson(response.body(), Auth.class);
                    authToken = authResponse.authToken();
                    ClientDraw.draw(args[0], state, args[1]);
                    state = State.POST_LOGIN;
                } else {
                    ClientDraw.printError("Login failed: " + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }

    private void registerHandler(String... args) {
        if (args.length != 4) {
            ClientDraw.printError("Usage: register <username> <password> <email>");
        } else {
            String username = args[1];
            String password = args[2];
            String email = args[3];
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/user"))
                    .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"" + username + "\", \"password\":\""
                            + password + "\", \"email\":\"" + email + "\"}"))
                    .header("Content-Type", "application/json")
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    Auth authResponse = gson.fromJson(response.body(), Auth.class);
                    authToken = authResponse.authToken();
                    ClientDraw.draw(args[0], state, args[1]);
                    state = State.POST_LOGIN;
                } else {
                    ClientDraw.printError("Register failed due to " + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }

    private void listHandler(String... args) {
        if (args.length != 1) {
            ClientDraw.printError("Usage: list");
        } else {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/game"))
                    .GET()
                    .header("Content-Type", "application/json")
                    .header("authToken", authToken)
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    Collection<GameData> games = gson.fromJson(response.body(), ListGames.class).games();
                    int index = 1;
                    String[] gameList = new String[games.size()];
                    for (GameData game : games) {
                        gameList[index-1] = "- " + index + ": " + game.gameName() + ", White: "
                                + (game.whiteUsername() != null ? game.whiteUsername() : "open")
                                + ", Black: " + (game.blackUsername() != null ? game.blackUsername() : "open");
                    }
                    ClientDraw.draw(args[0], state, gameList);
                } else {
                    ClientDraw.printError("List games failed due to " + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }
    private void joinHandler(String... args) {}
    private void createHandler(String... args) {}
    private void observeHandler(String... args) {}
    private void logoutHandler(String... args) {}
    private void leaveHandler(String... args) {}


}
