package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {

    GameData addWhitePlayer(String newWhitePlayer) {
        return new GameData((gameID), newWhitePlayer, (blackUsername), (gameName), (game));
    }

    GameData addBlackPlayer(String newBlackPlayer) {
        return new GameData((gameID), (whiteUsername), newBlackPlayer, (gameName), (game));
    }
}
