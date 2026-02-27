package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GameDAO {
    private Map<Integer, GameData> gameMap;
    private static int gameID;

    public GameDAO() {
        gameMap = new HashMap<>();
        gameID = 0;
    }

    private void increaseGameID() {
        gameID++;
    }

    public GameData createGame(String gameName) {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(gameID, null, null, gameName, chessGame);
        gameMap.put(gameID, game);
        increaseGameID();
        return game;
    }

    public GameData getGame(int id) throws DataAccessException {
        GameData game = gameMap.get(id);
        if (game == null) {
            throw new DataAccessException("Cannot get game because game ID does not map to a game: " + id);
        } else {
            return game;
        }
    }

    public Collection<GameData> listGames() {
        return gameMap.values();
    }

    public GameData joinGame(int id, String username, String teamColor) throws DataAccessException {
        GameData game = gameMap.get(id);
        if (game == null) {
            throw new DataAccessException("Cannot join game because game ID does not map to a game: " + id);
        } else if (teamColor.equals("white") && game.whiteUsername() != null) {
            return game.setWhitePlayer(username);
        } else if (teamColor.equals("black") && game.blackUsername() != null) {
            return game.setBlackPlayer(username);
        } else {
            throw new DataAccessException("Cannot join game because " + teamColor + " is taken. Game id: "  + id);
        }
    }

    public void clear() {
        gameMap = new HashMap<>();
    }
}
