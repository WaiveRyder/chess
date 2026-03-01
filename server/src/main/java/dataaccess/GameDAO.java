package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.*;

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

    public Collection<GameData> listGames() {
        return new Vector<>(gameMap.values());
    }

    public GameData joinGame(int id, String username, ChessGame.TeamColor teamColor) throws DataAccessException {
        GameData game = gameMap.get(id);
        if (game == null) {
            throw new DataAccessException("Cannot join game because game ID does not map to a game: " + id);
        } else if (teamColor == ChessGame.TeamColor.WHITE && game.whiteUsername() == null) {
            GameData newGame = game.setWhitePlayer(username);
            gameMap.put(id, newGame);
            return newGame;
        } else if (teamColor == ChessGame.TeamColor.BLACK && game.blackUsername() == null) {
            GameData newGame = game.setBlackPlayer(username);
            gameMap.put(id, newGame);
            return newGame;
        } else {
            throw new DataAccessException("Cannot join game because " + teamColor + " is taken. Game id: "  + id);
        }
    }

    public void clear() {
        gameMap = new HashMap<>();
    }
}
