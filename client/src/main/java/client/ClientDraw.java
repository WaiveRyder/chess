package client;

import ui.EscapeSequences;

public class ClientDraw {

    public void draw(String command, State state) {
        switch (state) {
            case PRE_LOGIN -> preLoginHandler(command);
            case POST_LOGIN -> postLoginHandler(command);
            case OBSERVE -> observeHandler(command);
        }
    }

    private void preLoginHandler(String command) {
        switch (command.toLowerCase()) {
            case "help" -> preLoginHelp();
        }
    }

    private void preLoginHelp() {
        System.out.println("Commands:");
        System.out.println("- login <username> <password>: Login with username and password");
        System.out.println("- register <username> <password> <email>: Register with username, password, and email");
        System.out.println("- quit: Exit the application");
        System.out.println("- help: Lists all commands");
    }

    private void postLoginHandler(String command) {
        switch (command.toLowerCase()) {
            case "help" -> postLoginHelp();
        }
    }

    private void postLoginHelp() {
        System.out.println("Commands:");
        System.out.println("- list: Lists all games");
        System.out.println("- join <id> [WHITE|BLACK]: Joins game with specified ID and color");
        System.out.println("- create <name>: Creates a new game with the specified name");
        System.out.println("- observe <id>: Observe a game with the specified ID");
        System.out.println("- logout: Logout from your account");
        System.out.println("- help: Lists all commands");
    }

    private void observeHandler(String command) {

    }
}
