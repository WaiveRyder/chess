package dataaccess;

import chess.ChessGame;
import dataaccess.offline.GameDAOMap;
import model.GameData;

import javax.xml.crypto.Data;
import java.util.*;

public class GameDAO {
    private DatabaseManager databaseManager;
    private GameDAOMap gameDAOMap;
    private boolean useMap;

    public GameDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        useMap = false;
    }

    public GameDAO(Map<Integer, GameData> gameMap) {
        gameDAOMap = new GameDAOMap(gameMap);
        useMap = true;
    }

    public GameData createGame(String gameName) {
        if (useMap) {
            return gameDAOMap.createGame(gameName);
        } else {
            return null;
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
