package service.requests;

import chess.ChessMove;

public record MakeMoveRequest(String authToken, Integer gameID, ChessMove move) {}
