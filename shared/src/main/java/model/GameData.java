package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {

    public GameData setWhitePlayer(String newWhitePlayer) {
        return new GameData((gameID), newWhitePlayer, (blackUsername), (gameName), (game));
    }

    public GameData setBlackPlayer(String newBlackPlayer) {
        return new GameData((gameID), (whiteUsername), newBlackPlayer, (gameName), (game));
    }
}
