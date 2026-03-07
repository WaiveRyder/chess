package dataaccess.offline;

import chess.ChessGame;
import dataaccess.DataAccessException;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class GameDAOMap {
    private Map<Integer, GameData> gameMap;
    private static Integer gameID;

    public GameDAOMap(Map<Integer, GameData> gameMap) {
        this.gameMap = gameMap;
        gameID = 1;
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

    public Collection<GameData> listGames() {
        return new Vector<>(gameMap.values());
    }

    public GameData joinGame(int id, String username, ChessGame.TeamColor teamColor) throws DataAccessException {
        GameData game = gameMap.get(id);
        if (game == null) {
            throw new DataAccessException("Error: Cannot join game because game ID does not map to a game: " + id);
        } else if (teamColor == ChessGame.TeamColor.WHITE && game.whiteUsername() == null) {
            GameData newGame = game.setWhitePlayer(username);
            gameMap.put(id, newGame);
            return newGame;
        } else if (teamColor == ChessGame.TeamColor.BLACK && game.blackUsername() == null) {
            GameData newGame = game.setBlackPlayer(username);
            gameMap.put(id, newGame);
            return newGame;
        } else {
            throw new DataAccessException("Error: Cannot join game because "+teamColor + " is taken. Game id: "  + id);
        }
    }

    public void clear() {
        gameMap.clear();
    }
}
