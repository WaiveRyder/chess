package client;

import java.net.http.HttpClient;

public class ServerFacade {
    int port;
    HttpClient client;

    public ServerFacade(int port) {
        this.port = port;
        client = HttpClient.newHttpClient();
    }

    public void request(String... args) {
        if (args.length == 0) {
            ClientDraw.printError("No command provided");
        } else {
            switch (args[0].toLowerCase()) {
                case "login" -> loginHandler();
                case "register" -> registerHandler();
                case "list" -> listHandler();
                case "join" -> joinHandler();
                case "create" -> createHandler();
                case "observe" -> observeHandler();
                case "logout" -> logoutHandler();
                case "leave" -> leaveHandler();
                default -> ClientDraw.printError("Unknown command: " + args[0]);
            }
        }
    }

    private void loginHandler() {}
    private void registerHandler() {}
    private void listHandler() {}
    private void joinHandler() {}
    private void createHandler() {}
    private void observeHandler() {}
    private void logoutHandler() {}
    private void leaveHandler() {}


}
