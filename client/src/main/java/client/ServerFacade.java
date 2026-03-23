package client;

import Responses.Auth;
import Responses.Game;
import Responses.ListGames;
import Responses.Message;
import chess.ChessBoard;
import chess.ChessGame;
import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import model.GameData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerFacade {
    private final int port;
    private final HttpClient client;
    private final Gson gson;
    private String authToken;
    public State state;
    private List<GameData> games;
    private Integer gameID;

    public ServerFacade(int port, State state) {
        this.port = port;
        client = HttpClient.newHttpClient();
        gson = new Gson();
        this.state = state;
        games = null;
    }

    public void request(String... args) {
        if (args.length == 0) {
            ClientDraw.printError("No command provided");
        } else {
            switch (args[0].toLowerCase()) {
                case "help", "quit" -> ClientDraw.draw(args[0], state);
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
        if (state == State.POST_LOGIN) {
            ClientDraw.printError("You are already logged in, please logout before trying to login again");
            return;
        } else if (state == State.OBSERVE) {
            ClientDraw.printError("You cannot login while observing, please leave the game and logout before trying to login");
            return;
        }

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
        if (state == State.POST_LOGIN) {
            ClientDraw.printError("You are already logged in, please logout before trying to register");
            return;
        } else if (state == State.OBSERVE) {
            ClientDraw.printError("You cannot register while observing, please leave the game and logout before trying to register");
            return;
        }

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
        if (state == State.PRE_LOGIN) {
            ClientDraw.printError("You must be logged in to list games");
            return;
        } else if (state == State.OBSERVE) {
            ClientDraw.printError("You cannot list games while observing, please leave the game you are observing before trying to list");
            return;
        }

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
                    ClientDraw.printError("List games failed due to " + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }

    private void joinHandler(String... args) {}

    private void createHandler(String... args) {
        if (state == State.PRE_LOGIN) {
            ClientDraw.printError("You must be logged in to create a game");
            return;
        } else if (state == State.OBSERVE) {
            ClientDraw.printError("You cannot create a game while observing, please leave the game you are observing before trying to create");
            return;
        }

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
                    ClientDraw.printError("Create game failed due to " + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }

    private void observeHandler(String... args) {
        if (state == State.PRE_LOGIN) {
            ClientDraw.printError("You must be logged in to observe a game");
            return;
        } else if (state == State.OBSERVE) {
            ClientDraw.printError("You are already observing a game, please leave the game you are currently observing before trying to observe another");
            return;
        }

        if (args.length != 2) {
            ClientDraw.printError("Usage: observe <game_id>");
        } else if (games == null) {
            ClientDraw.printError("You must list games before trying to observe!");
        } else {
            Integer givenGameID;
            try {
                givenGameID = Integer.parseInt(args[1]);
                gameID = games.get(givenGameID-1).gameID();
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
                    ChessBoard board = gson.fromJson(response.body(), Game.class).gameData().game().getBoard();
                    ClientDraw.drawBoard(board, ChessGame.TeamColor.WHITE);
                } else {
                    ClientDraw.printError("Observe game failed due to " + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }

    private void logoutHandler(String... args) {
        if (state != State.POST_LOGIN) {
            ClientDraw.printError("You must be logged in to logout");
            return;
        }

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
                    state = State.PRE_LOGIN;
                } else {
                    ClientDraw.printError("Logout failed due to " + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }

    private void leaveHandler(String... args) {
        if (state == State.PRE_LOGIN) {
            ClientDraw.printError("You must be logged in to leave a game");
            return;
        } else if (state == State.POST_LOGIN) {
            ClientDraw.printError("You must be observing a game to leave a game");
            return;
        }

        if (args.length != 1) {
            ClientDraw.printError("Usage: leave");
        } else {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/observe"))
                    .DELETE()
                    .header("Content-Type", "application/json")
                    .header("Authorization", authToken)
                    .header("gameID", String.valueOf(gameID))
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    ClientDraw.draw(args[0], state);
                    state = State.POST_LOGIN;
                } else {
                    ClientDraw.printError("Leaving game failed due to " + gson.fromJson(response.body(), Message.class).message());
                }
            } catch (Exception e) {
                ClientDraw.printError("Error: failed to connect to server, please try again");
            }
        }
    }


}
