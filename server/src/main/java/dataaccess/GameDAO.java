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

    public Collection<GameData> listGames() {
        if (useMap) {
            return gameDAOMap.listGames();
        } else {
            return null;
        }
    }

    public GameData joinGame(int id, String username, ChessGame.TeamColor teamColor) throws DataAccessException {
        if (useMap) {
            return gameDAOMap.joinGame(id, username, teamColor);
        } else {
            return null;
        }
    }

    public void clear() {
        if (useMap) {
            gameDAOMap.clear();
        }
    }
}
