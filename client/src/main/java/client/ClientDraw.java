package client;

import ui.EscapeSequences;

public class ClientDraw {

    // Main draw method that routes commands based on state
    public static void draw(String command, State state, String... args) {
        switch (state) {
            case PRE_LOGIN -> preLoginHandler(command, args);
            case POST_LOGIN -> postLoginHandler(command, args);
            case OBSERVE -> observeHandler(command);
        }
    }



    public static void printError(String message) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Hold up: " + message + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println("Type 'help' for a list of commands.");
    }



    // All pre-login commands are handled here ---------------------------------------
    private static void preLoginHandler(String command, String[] args) {
        switch (command.toLowerCase()) {
            case "help" -> preLoginHelp();
            case "quit" -> System.out.println("Thanks for playing! Catch you next time.");
            case "login" -> System.out.println("Successfully logged in. Welcome back " + args[0] + "!");
            case "register" -> System.out.println("Successfully registered! Welcome " + args[0] + "!");
        }
    }

    private static void preLoginHelp() {
        System.out.println("Commands:");
        System.out.println("- login <username> <password>: Login with username and password");
        System.out.println("- register <username> <password> <email>: Register with username, password, and email");
        System.out.println("- quit: Exit the application");
        System.out.println("- help: Lists all commands");
    }



    // All post-login commands are handled here ---------------------------------------
    private static void postLoginHandler(String command, String[] args) {
        switch (command.toLowerCase()) {
            case "help" -> postLoginHelp();
            case "list" -> listHandler(args);
            case "create" -> System.out.println("Successfully created game: " + args[0]);
            case "logout" -> System.out.println("Successfully logged out.");
            case "observe" -> System.out.println("Now observing game with ID: " + args[0]);
            case "quit" -> System.out.println(EscapeSequences.SET_TEXT_COLOR_RED+"Hold up: You must be logout first!"+EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private static void postLoginHelp() {
        System.out.println("Commands:");
        System.out.println("- list: Lists all games");
        System.out.println("- join <id> [WHITE|BLACK]: Joins game with specified ID and color");
        System.out.println("- create <name>: Creates a new game with the specified name");
        System.out.println("- observe <id>: Observe a game with the specified ID");
        System.out.println("- logout: Logout from your account");
        System.out.println("- help: Lists all commands");
    }

    private static void listHandler(String[] args) {
        System.out.println("Games:");
        for (String game : args) {
            System.out.println(game);
        }
    }



    // All observe commands are handled here ---------------------------------------
    private static void observeHandler(String command) {
        switch (command.toLowerCase()) {
            case "help" -> observeHelp();
            case "leave" -> System.out.println("Stopped observing game.");
            case "quit" -> System.out.println(EscapeSequences.SET_TEXT_COLOR_RED+"Hold up: You must be logout first!"+EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private static void observeHelp() {
        System.out.println("Commands:");
        System.out.println("- leave: Stop observing the game");
        System.out.println("- help: Lists all commands");
    }
}
