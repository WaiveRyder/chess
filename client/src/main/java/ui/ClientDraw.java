package ui;

import chess.*;
import client.State;

import java.util.Collection;

public class ClientDraw {

    // Main draw method that routes commands based on state
    public static void draw(String command, State state, String... args) {
        if (command.equalsIgnoreCase("clear")) {
            System.out.println("Cleared Databases");
        } else if (command.equalsIgnoreCase("message")) {
            System.out.println(args[0]);
        } else {
            switch (state) {
                case PRE_LOGIN -> preLoginHandler(command, args);
                case POST_LOGIN -> postLoginHandler(command, args);
                case OBSERVE -> observeHandler(command);
                case GAMEPLAY -> gameplayHandler(command, args);
            }
        }
    }



    public static void printError(String message) {
        if (message.contains("Error:")) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + message + EscapeSequences.RESET_TEXT_COLOR);
        } else {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + message
                    + EscapeSequences.RESET_TEXT_COLOR);
        }
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
            case "join" -> System.out.println("Successfully joined game with ID: " + args[0] + " as " + args[1]);
            case "logout" -> System.out.println("Successfully logged out.");
            case "observe" -> System.out.println("Now observing game with ID: " + args[0]);
        }
    }

    private static void postLoginHelp() {
        System.out.println("Commands:");
        System.out.println("- list: Lists all games");
        System.out.println("- join <id> [WHITE|BLACK]: Joins game with specified ID and color");
        System.out.println("- create <name>: Creates a new game with the specified name");
        System.out.println("- observe <id>: Observe a game with the specified ID");
        System.out.println("- logout: Logout from your account");
        System.out.println("- quit: Exit the application");
        System.out.println("- help: Lists all commands");
    }

    private static void listHandler(String[] args) {
        System.out.println("Games:");
        if (args.length == 0) {
            System.out.println("No games found, please create a new game");
        } else {
            for (String game : args) {
                System.out.println(game);
            }
        }
    }



    // All observe commands are handled here ---------------------------------------
    private static void observeHandler(String command) {
        switch (command.toLowerCase()) {
            case "help" -> observeHelp();
            case "leave" -> System.out.println("Stopped observing game.");
        }
    }

    private static void observeHelp() {
        System.out.println("Commands:");
        System.out.println("- redraw: redraws the board");
        System.out.println("- quit: Exit the application");
        System.out.println("- leave: Stop observing the game");
        System.out.println("- help: Lists all commands");
    }



    // All gameplay commands are handled here ---------------------------------------
    private static void gameplayHandler(String command, String[] args) {
        switch (command.toLowerCase()) {
            case "help" -> gamePlayHelp();
            case "confirm" -> System.out.println("Are you sure you want to resign? " +
                    "Type 'confirm' to resign or anything else to cancel");
            case "deny" -> System.out.println("Resignation cancelled. Continuing game");
            case "resign" -> System.out.println("Resigning from game");
            case "left" -> System.out.println("Resigned successfully");
            case "leave" -> System.out.println("Left game and returned to menu");
        }
    }

    private static void gamePlayHelp() {
        System.out.println("Commands:");
        System.out.println("- move <start> <end>: Move piece from start to end position (e.g. move e2 e4)");
        System.out.println("- highlight <position>: Highlights the piece at position and all valid moves (e.g. highlight e2)");
        System.out.println("- redraw: Redraws the board");
        System.out.println("- resign: Admit defeat and end the game");
        System.out.println("- leave: leave the current game");
        System.out.println("- quit: Exit the application");
        System.out.println("- help: Lists all commands");
    }


    // New line
    public static void highlightMoves(ChessGame game, ChessGame.TeamColor playerColor, ChessPosition pos) {
        if (game.getBoard().getPiece(pos) == null) {
            printError("Selected position does not contain a piece.");
            return;
        }

        int flipper = 0;
        if (playerColor == ChessGame.TeamColor.BLACK) {
            flipper = 9;
        }

        String[][] drawnBoard = assembleBoard(playerColor);
        String[][] placedPiecesBoard = placePieces(game.getBoard(), drawnBoard, playerColor);
        Collection<ChessMove> validMoves = game.validMoves(pos);
        for (ChessMove move : validMoves) {
            int row = Math.abs(flipper - move.getEndPosition().getRow());
            int col = Math.abs(flipper - move.getEndPosition().getColumn());
            ChessPiece piece = game.getBoard().getPiece(move.getEndPosition());
            String bgColor;
            if (placedPiecesBoard[9-row][col].contains("22m")) {
                bgColor = EscapeSequences.SET_BG_COLOR_DARK_GREY;
            } else {
                bgColor = EscapeSequences.SET_BG_COLOR_WHITE;
            }
            placedPiecesBoard[9-row][col] = bgColor
                    + EscapeSequences.SET_TEXT_COLOR_MAGENTA
                    + " " + (piece == null ? " " : piece.toUnicode()) + " "
                    + EscapeSequences.RESET_TEXT_COLOR
                    + EscapeSequences.RESET_BG_COLOR;
        }

        String piece = game.getBoard().getPiece(pos).toUnicode();
        int row = Math.abs(flipper - pos.getRow());
        int col = Math.abs(flipper - pos.getColumn());

        placedPiecesBoard[9-row][col] = EscapeSequences.SET_BG_COLOR_YELLOW
                + EscapeSequences.SET_TEXT_COLOR_MAGENTA
                + " " + piece + " " + EscapeSequences.RESET_BG_COLOR;
        print2D(placedPiecesBoard);
    }

    // Oh boy this is the big one
    public static void drawBoard(ChessBoard board, ChessGame.TeamColor playerColor) {
        String[][] drawnBoard = assembleBoard(playerColor);
        String[][] placedPiecesBoard = placePieces(board, drawnBoard, playerColor);
        print2D(placedPiecesBoard);
    }

    private static String[][] placePieces(ChessBoard board, String[][] drawnBoard, ChessGame.TeamColor playerColor) {
        String coloredPiece = "";

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece;
                if (playerColor == ChessGame.TeamColor.BLACK) {
                    piece = board.getPiece(new ChessPosition(row, 9 - col));
                } else {
                    piece = board.getPiece(new ChessPosition(9 - row, col));
                }
                String currentBox = drawnBoard[row][col];
                if (piece != null && currentBox.contains("12m")) {
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        coloredPiece = EscapeSequences.SET_TEXT_COLOR_WHITE + piece.toUnicode()
                                + EscapeSequences.RESET_TEXT_COLOR;
                    } else {
                        coloredPiece = EscapeSequences.SET_TEXT_COLOR_BLACK + piece.toUnicode()
                                + EscapeSequences.RESET_TEXT_COLOR;
                    }
                    drawnBoard[row][col] = chessSquareWhite(coloredPiece);
                } else if (piece != null) {
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        coloredPiece = EscapeSequences.SET_TEXT_COLOR_WHITE + piece.toUnicode()
                                + EscapeSequences.RESET_TEXT_COLOR;
                    } else {
                        coloredPiece = EscapeSequences.SET_TEXT_COLOR_BLACK + piece.toUnicode()
                                + EscapeSequences.RESET_TEXT_COLOR;
                    }
                    drawnBoard[row][col] = chessSquareBlack(coloredPiece);
                }
            }
        }
        return drawnBoard;
    }

    private static String[][] assembleBoard(ChessGame.TeamColor playerColor) {
        String[][] board = new String[10][10];
        board[0] = createBoarderRow(playerColor);
        board[9] = createBoarderRow(playerColor);

        for (int i = 1; i <= 8; i++) {
            board[i] = createSquareRows(i, playerColor);
        }

        return board;
    }

    private static String[] createSquareRows(int rowNum, ChessGame.TeamColor playerColor) {
        String[] row = new String[10];
        if (playerColor == ChessGame.TeamColor.BLACK) {
            row[0] = boarderBox(String.valueOf(rowNum));
            row[9] = boarderBox(String.valueOf(rowNum));
        } else {
            row[0] = boarderBox(String.valueOf(9 - rowNum));
            row[9] = boarderBox(String.valueOf(9 - rowNum));
        }

        for (int i = 1; i <= 8; i++) {
            if (rowNum % 2 == 0) {
                if (i % 2 == 0) {
                    row[i] = chessSquareWhite(" ");
                } else {
                    row[i] = chessSquareBlack(" ");
                }
            } else {
                if (i % 2 == 0) {
                    row[i] = chessSquareBlack(" ");
                } else {
                    row[i] = chessSquareWhite(" ");
                }
            }
        }
        return row;
    }

    private static String[] createBoarderRow(ChessGame.TeamColor playerColor) {
        String[] boarder = new String[10];
        String[] boarderChars = {" ", "a", "b", "c", "d", "e", "f", "g", "h", " "};

        if (playerColor == ChessGame.TeamColor.BLACK) {
            boarderChars = new String[]{" ", "h", "g", "f", "e", "d", "c", "b", "a", " "};
        }
        for (int i = 0; i < 10; i++) {
            String singleBox = boarderBox(boarderChars[i]);
            boarder[i] = singleBox;
        }
        return boarder;
    }

    private static void print1D(String[] array) {
        for (String s: array) {
            System.out.print(s);
        }
        System.out.println();
    }

    private static void print2D(String[][] array) {
        for (String[] s: array) {
            print1D(s);
        }
    }

    private static String boarderBox(String boxChar) {
        return EscapeSequences.SET_BG_COLOR_LIGHT_GREY
                + EscapeSequences.SET_TEXT_COLOR_BLACK
                + " " + boxChar + " "
                + EscapeSequences.RESET_BG_COLOR
                + EscapeSequences.RESET_TEXT_COLOR;
    }
    private static String chessSquareWhite(String piece) {
        return EscapeSequences.SET_BG_COLOR_BLUE
                + " " + piece + " "
                + EscapeSequences.RESET_BG_COLOR;
    }

    private static String chessSquareBlack(String piece) {
        return EscapeSequences.SET_BG_COLOR_DARK_GREEN
                + " " + piece + " "
                + EscapeSequences.RESET_BG_COLOR;
    }
}
