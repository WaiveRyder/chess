package dataaccess;

import chess.ChessGame;
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
                    throw new DataAccessException("Error: Could not create game");
                }

                int id = -1;
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        id = rs.getInt(1);
                    }
                }

                return new GameData(id, null, null, gameName, newGame);
            } catch (SQLException e) {
                throw new DataAccessException("Error: could not connect to database");
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
                throw new DataAccessException("Error: could not connect to database");
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
                    if(teamColor == ChessGame.TeamColor.WHITE && whiteUsername != null) {
                        throw new DataAccessException("Error: "+teamColor + "is taken by: " + whiteUsername);
                    } else if(teamColor == ChessGame.TeamColor.BLACK && blackUsername != null) {
                        throw new DataAccessException("Error: "+teamColor + "is taken by: " + blackUsername);
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
                        throw new DataAccessException("Error: Could not update game");
                    }
                    return gameData;
                } else {
                    throw new DataAccessException("Error: Game ID not valid: " + id);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: could not connect to database");
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
                    throw new DataAccessException("Error: Game ID not valid: " + id);
                }
            }

            statement = "INSERT INTO observers (gameID, username) VALUES (?, ?)";
            try (PreparedStatement nnpstmt = conn.prepareStatement(statement)) {
                nnpstmt.setInt(1, id);
                nnpstmt.setString(2, username);
                int rows = nnpstmt.executeUpdate();
                if (rows == 0) {
                    throw new DataAccessException("Error: Could not add observer to game");
                } else {
                    return gameData;
                }

            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: could not connect to database");
        }
    }

    public void leaveObserveGame(int id, String username) throws DataAccessException {
        var statement = "SELECT username FROM observers WHERE gameID = ? AND username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(statement)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    statement = "DELETE FROM observers WHERE gameID = ? AND username = ?";
                    try (PreparedStatement nnpstmt = conn.prepareStatement(statement)) {
                        nnpstmt.setInt(1, id);
                        nnpstmt.setString(2, username);
                        int rows = nnpstmt.executeUpdate();
                        if (rows == 0) {
                            throw new DataAccessException("Error: Could not remove observer from game");
                        }
                    }
                } else {
                    throw new DataAccessException("Error: User is not an observer of this game");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: could not connect to database");
        }
    }

    public void clear() throws DataAccessException {
        if (useMap) {
            gameDAOMap.clear();
        } else {
            var statement = "TRUNCATE TABLE game";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(statement)) {
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new DataAccessException("Error: could not connect to database");
            }
        }
    }
}
