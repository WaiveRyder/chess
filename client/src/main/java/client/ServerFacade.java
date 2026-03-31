package client;

import chess.*;
import responses.Auth;
import responses.Game;
import responses.ListGames;
import responses.Message;
import com.google.gson.Gson;
import model.GameData;
import ui.ClientDraw;
import websocket.commands.UserGameCommand;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static client.State.*;

public class ServerFacade {
    private final int port;
    private final HttpClient client;
    private ClientWS ws;

    private final Gson gson;
    private String authToken;
    public State state;

    private List<GameData> games;
    private Integer gameID;
    private ChessGame game;
    private ChessGame.TeamColor playerColor;

    private boolean confirmResign;

    public ServerFacade(int port) {
        this.port = port;
        client = HttpClient.newHttpClient();
        gson = new Gson();
        state = PRE_LOGIN;
        games = null;
        ws = null;
        confirmResign = false;
    }

    public void request(String... args) {
        if (args.length == 0) {
            ClientDraw.printError("No command provided");
        } else {
            String command = args[0].toLowerCase();
            if (command.equals("help")) {
                helpHandler(args);
                return;
            } else if (command.equals("quit")) {
                quitHandler(args);
                return;
            }

            if (state == PRE_LOGIN) {
                switch (command) {
                    case "login" -> loginHandler(args);
                    case "register" -> registerHandler(args);
                    default -> ClientDraw.printError("Unknown command: " + args[0]);
                }
            } else if (state == POST_LOGIN) {
                switch (command) {
                    case "list" -> listHandler(args);
                    case "create" -> createHandler(args);
                    case "join" -> joinHandler(args);
                    case "observe" -> observeHandler(args);
                    case "logout" -> logoutHandler(args);
                    default -> ClientDraw.printError("Unknown command: " + args[0]);
                }
            } else if (state == OBSERVE) {
                switch (command) {
                    case "leave" -> leaveHandler(args);
                    case "redraw" -> ClientDraw.drawBoard(game.getBoard(), playerColor);
                    case "highlight" -> highlightHandler(args);
                    default -> ClientDraw.printError("Unknown command: " + args[0]);
                }
            } else if (state == GAMEPLAY) {
                switch (command) {
                    case "redraw" -> ClientDraw.drawBoard(game.getBoard(), playerColor);
                    case "move" -> moveHandler(args);
                    case "highlight" -> highlightHandler(args);
                    case "leave" -> leaveHandler(args);
                    case "resign" -> resignHandler(args);
                    case "confirm" -> resignHandler(args);
                    default -> {
                        if (confirmResign) {
                            ClientDraw.draw("deny", state);
                            confirmResign = false;
                        } else {
                            ClientDraw.printError("Unknown command: " + args[0]);
                        }
                    }
                }
            }
        }
    }

    private void highlightHandler(String... args) {
        if (args.length != 2) {
            ClientDraw.printError("Usage: highlight <position>");
        } else {
            ChessPosition pos = checkMoveFormat(args[1]);
            if (pos == null) {
                ClientDraw.printError("Position must be in format <a-h><1-8>");
            } else {
                ClientDraw.highlightMoves(game, playerColor, pos);
            }
        }
    }

    private ChessPosition checkMoveFormat(String move) {
        if (move.length() != 2) {
            return null;
        } else if (!Character.isDigit(move.charAt(1))) {
            return null;
        } else {
            int row = Character.getNumericValue(move.charAt(1));
            int col = move.charAt(0) - 'a' + 1;
            if (col < 1 || col > 8 || row < 1 || row > 8) {
                return null;
            } else {
                return new ChessPosition(row, col);
            }
        }
    }

    private void helpHandler(String... args) {
        if (args.length != 1) {
            ClientDraw.printError("Usage: help");
        } else {
            ClientDraw.draw(args[0], state);
        }
    }

    private void quitHandler(String... args) {
        if (args.length != 1) {
            ClientDraw.printError("Usage: quit");
        } else {
            if (state == PRE_LOGIN) {
                ClientDraw.draw(args[0], state);
                System.exit(0);
            } else if (state == POST_LOGIN) {
                logoutHandler("logout");
                ClientDraw.draw(args[0], state);
                System.exit(0);
            } else if (state == OBSERVE || state == GAMEPLAY) {
                leaveHandler("leave");
                logoutHandler("logout");
                ClientDraw.draw(args[0], state);
                System.exit(0);
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
                    .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"" + username + "\"," +
                            " \"password\":\"" + password + "\"}"))
                    .header("Content-Type", "application/json")
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    authResponse(gson.fromJson(response.body(), Auth.class), args[0], args[1]);

                } else {
                    ClientDraw.printError("Login failed: " + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }

    private void authResponse(Auth authResponse, String command, String username) {
        authToken = authResponse.authToken();
        ClientDraw.draw(command, state, username);
        state = State.POST_LOGIN;
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
                    .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"" + username + "\", " +
                            "\"password\":\"" + password + "\", \"email\":\"" + email + "\"}"))
                    .header("Content-Type", "application/json")
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    authResponse(gson.fromJson(response.body(), Auth.class), args[0], args[1]);
                } else {
                    ClientDraw.printError("Register failed due to "
                            + gson.fromJson(response.body(), Message.class).message());
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
                    .header("Authorization", authToken)
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
                        index++;
                    }
                    this.games = new ArrayList<>(games);
                    ClientDraw.draw(args[0], state, gameList);
                } else {
                    ClientDraw.printError("List games failed due to "
                            + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }

    private void joinHandler(String... args) {
        if (args.length != 3) {
            ClientDraw.printError("Usage: join <game_id> [WHITE|BLACK]");
        } else if (games == null) {
            ClientDraw.printError("You must list games before trying to join!");
        } else {
            Integer givenGameID;
            try {
                givenGameID = Integer.parseInt(args[1]);
                if (givenGameID < 1 || givenGameID > games.size()) {
                    if (games.isEmpty()) {
                        ClientDraw.printError("There are no games to join, please create a game" +
                                " before trying to join");
                    } else {
                        ClientDraw.printError("Game ID must be between 1 and " + games.size());
                    }
                    return;
                } else {
                    gameID = games.get(givenGameID-1).gameID();
                }
            } catch (NumberFormatException e) {
                ClientDraw.printError("Game ID must be an integer");
                return;
            }

            ChessGame.TeamColor color;
            if (args[2].equalsIgnoreCase("white")) {
                color = ChessGame.TeamColor.WHITE;
            } else if (args[2].equalsIgnoreCase("black")) {
                color = ChessGame.TeamColor.BLACK;
            } else {
                ClientDraw.printError("Color must be either WHITE or BLACK");
                return;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/game"))
                    .PUT(HttpRequest.BodyPublishers.ofString("{\"gameID\":\"" + gameID + "\"," +
                            " \"playerColor\":\"" + color + "\"}"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", authToken)
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    ClientDraw.draw(args[0], state, String.valueOf(givenGameID), color.toString());
                    state = State.GAMEPLAY;
                    game = gson.fromJson(response.body(), Game.class).gameData().game();
                    ws = new ClientWS(port);
                    ws.connect(authToken, gameID, " as " + color);
                    playerColor = color;
                    ws.setColor(color);
                    //ClientDraw.drawBoard(game.getBoard(), color);
                } else {
                    ClientDraw.printError("Join game failed due to "
                            + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }

    private void createHandler(String... args) {
        if (args.length != 2) {
            ClientDraw.printError("Usage: create <game_name> (no spaces allowed in name)");
        } else {
            String gameName = args[1];
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/game"))
                    .POST(HttpRequest.BodyPublishers.ofString("{\"gameName\":\"" + gameName + "\"}"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", authToken)
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    ClientDraw.draw(args[0], state, gameName);
                } else {
                    ClientDraw.printError("Create game failed due to "
                            + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }

    private void observeHandler(String... args) {
        if (args.length != 2) {
            ClientDraw.printError("Usage: observe <game_id>");
        } else if (games == null) {
            ClientDraw.printError("You must list games before trying to observe!");
        } else {
            Integer givenGameID;
            try {
                givenGameID = Integer.parseInt(args[1]);
                if (givenGameID < 1 || givenGameID > games.size()) {
                    ClientDraw.printError("Game ID must be between 1 and " + games.size());
                    return;
                } else {
                    gameID = games.get(givenGameID-1).gameID();
                }
            } catch (NumberFormatException e) {
                ClientDraw.printError("Game ID must be an integer");
                return;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/observe"))
                    .GET()
                    .header("Content-Type", "application/json")
                    .header("Authorization", authToken)
                    .header("gameID", String.valueOf(gameID))
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    ClientDraw.draw(args[0], state, String.valueOf(givenGameID));
                    state = State.OBSERVE;
                    game = gson.fromJson(response.body(), Game.class).gameData().game();
                    playerColor = ChessGame.TeamColor.WHITE;
                    ws = new ClientWS(port);
                    ws.connect(authToken, gameID, " as observer");
                    ws.setColor(ChessGame.TeamColor.WHITE);
                    ClientDraw.drawBoard(game.getBoard(), ChessGame.TeamColor.WHITE);
                } else {
                    ClientDraw.printError("Observe game failed due to "
                            + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }

    private void logoutHandler(String... args) {
        if (args.length != 1) {
            ClientDraw.printError("Usage: logout");
        } else {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/session"))
                    .DELETE()
                    .header("Authorization", authToken)
                    .header("Content-Type", "application/json")
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    ClientDraw.draw(args[0], state);
                    state = PRE_LOGIN;
                } else {
                    ClientDraw.printError("Logout failed due to "
                            + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }

    private void leaveHandler(String... args) {
        if (args.length != 1) {
            ClientDraw.printError("Usage: leave");
        } else {
            try {
                ws.leave(authToken, gameID);
                ws.close();
                ClientDraw.draw(args[0], state);
                state = State.POST_LOGIN;
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }

    private void moveHandler(String... args) {
        if (args.length != 3 && args.length != 4) {
            ClientDraw.printError("Usage: move <start> <end> [promotion_piece]");
        } else {
            ChessPosition startPos = checkMoveFormat(args[1]);
            ChessPosition endPos = checkMoveFormat(args[2]);
            if (startPos == null || endPos == null) {
                ClientDraw.printError("Start position must be in format <a-h><1-8>");
            } else {
                ChessMove move;
                if (args.length == 4 && checkPromotionFormat(args[3]) == null) {
                    ClientDraw.printError("Promotion piece must be either a pawn, " +
                            "rook, knight, bishop, queen, or king");
                    return;
                } else {
                    move = new ChessMove(startPos, endPos, checkPromotionFormat(args[3]));
                }
                try {
                    String message = args[1] + " " + args[2] + (args.length == 4 ? " " + args[3] : "");
                    ws.makeMove(authToken, gameID, message, move);
                } catch (Exception e) {
                    ClientDraw.printError("Error: failed to connect to server, please try again");
                }
            }
        }
    }

    private ChessPiece.PieceType checkPromotionFormat(String promotion) {
        promotion = promotion.toLowerCase();
        switch (promotion) {
            case "pawn" -> {return ChessPiece.PieceType.PAWN;}
            case "rook" -> {return ChessPiece.PieceType.ROOK;}
            case "knight" -> {return ChessPiece.PieceType.KNIGHT;}
            case "bishop" -> {return ChessPiece.PieceType.BISHOP;}
            case "queen" -> {return ChessPiece.PieceType.QUEEN;}
            default -> {return null;}
        }
    }

    private void resignHandler(String... args) {
        if (args.length != 1) {
            ClientDraw.printError("Usage: resign");
            return;
        } else if (args[0].equalsIgnoreCase("resign") && !confirmResign) {
            ClientDraw.draw("confirm", state);
            confirmResign = true;
            return;
        } else if (args[0].equalsIgnoreCase("resign") && confirmResign) {
            ClientDraw.draw("deny", state);
            confirmResign = false;
            return;
        } else if (args[0].equalsIgnoreCase("confirm") && !confirmResign) {
            ClientDraw.printError("Unknown command: " + args[0]);
            return;
        } else {
            ClientDraw.draw("resign", state);
            confirmResign = false;
        }

        try {
            ws.resign(authToken, gameID);
        } catch (Exception e) {
            ClientDraw.printError("Error: failed to connect to server, please try again");
        }
    }

    public void clear() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/db"))
                .DELETE()
                .header("Content-Type", "application/json")
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
            ClientDraw.draw("clear", state);
        } catch (Exception e) {
            System.out.println("Error: failed to connect to server, please try again");
        }
    }

}
