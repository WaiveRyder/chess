package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.offline.GameDAOMap;
import model.GameData;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.*;

public class GameDAO {
    private GameDAOMap gameDAOMap;
    private boolean useMap;
    Gson gson;

    public GameDAO() {
        useMap = false;
        gson = new Gson();
    }

    public GameDAO(Map<Integer, GameData> gameMap) {
        gameDAOMap = new GameDAOMap(gameMap);
        useMap = true;
    }

    public GameData createGame(String gameName) throws DataAccessException {
        if (useMap) {
            return gameDAOMap.createGame(gameName);
        } else {
            if (gameName == null || gameName.isEmpty()) {
                throw new DataAccessException("Error: Game name cannot be null");
            }

            var statement = "INSERT INTO game (whiteUsername, blackUsername, gameName, chessGame) VALUES (" +
                    "null, null, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, gameName);
                ChessGame newGame = new ChessGame();
                String serialize = gson.toJson(newGame);
                pstmt.setString(2, serialize);

                int rows = pstmt.executeUpdate();
                if (rows == 0) {
                    throw new DataAccessException("Error: Could not create game. Please try again later.");
                }

                int id = -1;
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        id = rs.getInt(1);
                    }
                }

                return new GameData(id, null, null, gameName, newGame);
            } catch (SQLException e) {
                throw new DataAccessException("Error: could not connect to the database. Please try again later.");
            }
        }
    }

    public Collection<GameData> listGames() throws DataAccessException {
        if (useMap) {
            return gameDAOMap.listGames();
        } else {
            var statement = "SELECT * FROM game";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(statement);
                 ResultSet rs = pstmt.executeQuery()) {
                Vector<GameData> games = new Vector<>();
                while (rs.next()) {
                    int gameID = rs.getInt("gameID");
                    String whiteUsername = rs.getString("whiteUsername");
                    String blackUsername = rs.getString("blackUsername");
                    String gameName = rs.getString("gameName");
                    ChessGame chessGame = gson.fromJson(rs.getString("chessGame"), ChessGame.class);
                    GameData newGameData = new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
                    games.add(newGameData);
                }
                return games;
            } catch (SQLException e) {
                throw new DataAccessException("Error: could not connect to the database. Please try again later.");
            }
        }
    }

    public GameData joinGame(int id, String username, ChessGame.TeamColor teamColor) throws DataAccessException {
        if (useMap) {
            return gameDAOMap.joinGame(id, username, teamColor);
        }
        var statement = "SELECT * FROM game WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(statement)) {
            pstmt.setInt(1, id);
            GameData gameData = null;
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int gameID = rs.getInt("gameID");
                    String whiteUsername = rs.getString("whiteUsername");
                    String blackUsername = rs.getString("blackUsername");
                    String gameName = rs.getString("gameName");
                    ChessGame chessGame = gson.fromJson(rs.getString("chessGame"), ChessGame.class);
                    gameData = new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
                    var newStatement = "";
                    if(teamColor == ChessGame.TeamColor.WHITE
                            && !Objects.equals(whiteUsername, username)
                            && whiteUsername != null) {
                        throw new DataAccessException("Error: "+teamColor + " is taken by: " + whiteUsername);
                    } else if(teamColor == ChessGame.TeamColor.BLACK
                            && !Objects.equals(blackUsername, username)
                            && blackUsername != null) {
                        throw new DataAccessException("Error: "+teamColor + " is taken by: " + blackUsername);
                    } else if (teamColor == ChessGame.TeamColor.WHITE) {
                        gameData = gameData.setWhitePlayer(username);
                        newStatement = "UPDATE game SET whiteUsername = ? WHERE gameID = " + gameID;
                    } else {
                        gameData = gameData.setBlackPlayer(username);
                        newStatement = "UPDATE game SET blackUsername = ? WHERE gameID = " + gameID;
                    }

                    int rows;
                    try (PreparedStatement npstmt = conn.prepareStatement(newStatement)) {
                        npstmt.setString(1, username);
                        rows = npstmt.executeUpdate();
                    }
                    if (rows == 0) {
                        throw new DataAccessException("Error: Could not update game. Please try again later.");
                    }
                    return gameData;
                } else {
                    throw new DataAccessException("Error: Game ID not valid. Please refresh and try again.");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: could not connect to the database. Please try again later.");
        }
    }

    public void leaveGame(int id, ChessGame.TeamColor teamColor) throws DataAccessException {
        if (useMap) {
            //Not implemented
            return;
        }
        var statement = "";
        if (teamColor == ChessGame.TeamColor.WHITE) {
            statement = "UPDATE game SET whiteUsername = null WHERE gameID = ?";
        } else {
            statement = "UPDATE game SET blackUsername = null WHERE gameID = ?";
        }
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(statement)) {
            pstmt.setInt(1, id);
            if (pstmt.executeUpdate() == 0) {
                throw new DataAccessException("Error: Could not update game. Please try again later.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: could not connect to the database. Please try again later.");
        }
    }

    public GameData observeGame(int id, String username) throws DataAccessException {
        if (useMap) {
            //Implement observe game for map if we have time
        }

        var statement = "SELECT * FROM game WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(statement)) {
            pstmt.setInt(1, id);
            GameData gameData = null;
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int gameID = rs.getInt("gameID");
                    String whiteUsername = rs.getString("whiteUsername");
                    String blackUsername = rs.getString("blackUsername");
                    String gameName = rs.getString("gameName");
                    ChessGame chessGame = gson.fromJson(rs.getString("chessGame"), ChessGame.class);
                    gameData = new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
                } else {
                    throw new DataAccessException("Error: Game ID not valid. Please refresh and try again.");
                }
            }

            statement = "INSERT INTO observers (gameID, username) VALUES (?, ?)";
            try (PreparedStatement nnpstmt = conn.prepareStatement(statement)) {
                nnpstmt.setInt(1, id);
                nnpstmt.setString(2, username);
                int rows = nnpstmt.executeUpdate();
                if (rows == 0) {
                    throw new DataAccessException("Error: Could not add observer to game. Please try again later.");
                } else {
                    return gameData;
                }

            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: could not connect to the database. Please try again later.");
        }
    }

    public void leaveObserveGame(int id, String username) throws DataAccessException {
        var statement = "DELETE FROM observers WHERE gameID = ? AND username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(statement)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, username);
            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                throw new DataAccessException("Error: Could not remove observer from game. Try again.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: could not connect to the database. Please try again later.");
        }
    }

    public void clear() throws DataAccessException {
        if (useMap) {
            gameDAOMap.clear();
        } else {
            var statement = "DROP TABLE observers";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(statement)) {
                pstmt.executeUpdate();
                try (PreparedStatement npstmt = conn.prepareStatement("TRUNCATE TABLE game")) {
                    npstmt.executeUpdate();
                }

                var createObserversStatement = """
                CREATE TABLE IF NOT EXISTS observers (
                gameID int NOT NULL,
                username VARCHAR(255) NOT NULL,
                FOREIGN KEY (gameID) REFERENCES game(gameID),
                FOREIGN KEY (username) REFERENCES users(username)
                )
                """;

                try (PreparedStatement p = conn.prepareStatement(createObserversStatement)) {
                    p.executeUpdate();
                }
            } catch (SQLException e) {
                throw new DataAccessException("Error: could not connect to the database. Please try again later.");
            }
        }
    }

    public GameData makeMove(int gameID, String username, ChessMove move) throws DataAccessException {
        if (useMap) {
            //Implement
            return null;
        }
        var statement = "SELECT * FROM game WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(statement)) {
            pstmt.setInt(1, gameID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ChessGame gameData = gson.fromJson(rs.getString("chessGame"), ChessGame.class);
                ChessGame.TeamColor color;
                String whiteUsername = rs.getString("whiteUsername");
                String blackUsername = rs.getString("blackUsername");

                if (whiteUsername != null && whiteUsername.equals(username)) {
                    color = ChessGame.TeamColor.WHITE;
                } else if (blackUsername != null && blackUsername.equals(username)) {
                    color = ChessGame.TeamColor.BLACK;
                } else {
                    throw new DataAccessException("Error: User is not a player in this game");
                }

                ChessPiece piece = gameData.getBoard().getPiece(move.getStartPosition());
                if (piece != null && piece.getTeamColor() != color && !gameData.gameOver) {
                    throw new DataAccessException("Error: Cannot move opponent's piece");
                } else if (gameData.gameOver) {
                    throw new DataAccessException("Error: Game is over, no moves can be made");
                }

                gameData.makeMove(move);
                var updateStatement = "UPDATE game SET chessGame = ? WHERE gameID = ?";
                PreparedStatement updatePstmt = conn.prepareStatement(updateStatement);
                String serialize = gson.toJson(gameData);
                updatePstmt.setString(1, serialize);
                updatePstmt.setInt(2, gameID);
                int rows = updatePstmt.executeUpdate();
                if (rows == 0) {
                    throw new DataAccessException("Error: Could not update game. Please try again later.");
                } else {
                    return new GameData(gameID,whiteUsername, blackUsername, rs.getString("gameName"), gameData);
                }

            } else {
                throw new DataAccessException("Error: Game ID not valid. Please refresh and try again.");
            }
        } catch (SQLException | InvalidMoveException e) {
            if (e instanceof SQLException) {
                throw new DataAccessException("Error: could not connect to the database. Please try again later.");
            } else {
                throw new DataAccessException(e.getMessage());
            }
        }
    }

    public GameData getGame(int gameID) throws DataAccessException {
        var statement = "SELECT * FROM game WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(statement)) {
            pstmt.setInt(1, gameID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                ChessGame game = gson.fromJson(rs.getString("chessGame"), ChessGame.class);
                String whiteUsername = rs.getString("whiteUsername");
                String blackUsername = rs.getString("blackUsername");
                return new GameData(gameID, whiteUsername, blackUsername, rs.getString("gameName"), game);
            } else {
                throw new DataAccessException("Error: Game ID not valid. Please refresh and try again.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: could not connect to the database. Please try again later.");
        }
    }
}
