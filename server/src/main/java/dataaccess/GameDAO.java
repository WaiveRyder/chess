package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.offline.GameDAOMap;
import model.GameData;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.*;

public class GameDAO {
    private DatabaseManager databaseManager;
    private GameDAOMap gameDAOMap;
    private boolean useMap;
    Gson gson;

    public GameDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
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
                throw new DataAccessException("could not connect to database", e);
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
                throw new DataAccessException("could not connect to database", e);
            }
        }
    }

    public GameData joinGame(int id, String username, ChessGame.TeamColor teamColor) throws DataAccessException {
        if (useMap) {
            return gameDAOMap.joinGame(id, username, teamColor);
        } else {
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
                            whiteUsername = username;
                            newStatement = "UPDATE game SET whiteUsername = ? WHERE gameID = " + gameID;
                        } else {
                            blackUsername = username;
                            newStatement = "UPDATE game SET blackUsername = ? WHERE gameID = " + gameID;
                        }

                        int rows;
                        try (PreparedStatement npstmt = conn.prepareStatement(newStatement)) {
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
                throw new DataAccessException("could not connect to database", e);
            }
        }
    }

    public void clear() {
        if (useMap) {
            gameDAOMap.clear();
        }
    }
}
