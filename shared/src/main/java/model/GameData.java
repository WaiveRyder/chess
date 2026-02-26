package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {

    private static int gameIDCounter = 0;

    public GameData(String whiteUsername, String blackUsername, String gameName, ChessGame game) {
        this(gameIDCounter, whiteUsername, blackUsername, gameName, game);
        incrementGameID();
    }

    private void incrementGameID() {
        gameIDCounter++;
    }

    GameData addWhitePlayer(String newWhitePlayer) {
        return new GameData((gameID), newWhitePlayer, (blackUsername), (gameName), (game));
    }

    GameData addBlackPlayer(String newBlackPlayer) {
        return new GameData((gameID), (whiteUsername), newBlackPlayer, (gameName), (game));
    }
}
