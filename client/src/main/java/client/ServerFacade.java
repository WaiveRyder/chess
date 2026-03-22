package client;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    int port;
    HttpClient client;
    Gson gson;

    public ServerFacade(int port) {
        this.port = port;
        client = HttpClient.newHttpClient();
        gson = new Gson();
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
                    ClientDraw.draw(args[0], State.PRE_LOGIN);
                } else {
                    ClientDraw.printError("Login failed: " + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Failed to connect to server, please try again");
            }
        }
    }

    private void registerHandler(String... args) {}
    private void listHandler(String... args) {}
    private void joinHandler(String... args) {}
    private void createHandler(String... args) {}
    private void observeHandler(String... args) {}
    private void logoutHandler(String... args) {}
    private void leaveHandler(String... args) {}


}
