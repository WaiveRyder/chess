package service;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import service.requests.*;
import service.responses.CreateGameResponse;
import service.responses.GenericResponse;
import service.responses.ListGamesResponse;
import service.responses.ReturnGameResponse;

public class GameService {
    private AuthDAO authDAO;
    private GameDAO gameDAO;

    public GameService (AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public GenericResponse clear() {
        try {
            gameDAO.clear();
            return new GenericResponse("");
        } catch (DataAccessException e) {
            return new GenericResponse(e.getMessage());
        }
    }

    private AuthData authenticate(String token) throws DataAccessException {
        return authDAO.getAuthData(token);
    }

    public CreateGameResponse createGame(CreateGameRequest request) {
        try {
            AuthData auth = authenticate(request.authToken());
            GameData newGame = gameDAO.createGame(request.gameName());
            return new CreateGameResponse(newGame.gameID(), "");
        } catch (DataAccessException e) {
            return new CreateGameResponse(null, e.getMessage());
        }
    }

    public ReturnGameResponse joinGame(JoinGameRequest request) {
        try {
            AuthData auth = authenticate(request.authToken());
            GameData game = gameDAO.joinGame(request.gameID(), auth.username(), request.playerColor());
            return new ReturnGameResponse(game, "");
        } catch (DataAccessException e) {
            return new ReturnGameResponse(null, e.getMessage());
        }
    }

    public GenericResponse leaveGame(JoinGameRequest request) {
        try {
            AuthData auth = authenticate(request.authToken());
            gameDAO.leaveGame(request.gameID(), request.playerColor());
            return new GenericResponse("");
        } catch (DataAccessException e) {
            return new GenericResponse(e.getMessage());
        }
    }

    public ListGamesResponse listGames(AuthRequest request) {
        try {
            AuthData auth = authenticate(request.authToken());
            return new ListGamesResponse(gameDAO.listGames(), "");
        } catch (DataAccessException e) {
            return new ListGamesResponse(null, e.getMessage());
        }
    }

    public ReturnGameResponse observeGame(ObserveGameRequest request) {
        try {
            AuthData auth = authenticate(request.token());
            if (request.leave()) {
                gameDAO.leaveObserveGame(request.gameID(), auth.username());
                return new ReturnGameResponse(null, "");
            } else {
                GameData game = gameDAO.observeGame(request.gameID(), auth.username());
                return new ReturnGameResponse(game, "");
            }
        } catch (DataAccessException e) {
            return new ReturnGameResponse(null,  e.getMessage());
        }
    }

    public ReturnGameResponse makeMove(MakeMoveRequest request) {
        try {
            AuthData auth = authenticate(request.authToken());

            String[] moveRequest = request.move().split(" ");
            ChessPiece.PieceType promotion = null;
            if (moveRequest.length != 4 && moveRequest.length != 5) {
                return new ReturnGameResponse(null, "Error: Invalid move format");
            } else if (moveRequest.length == 5) {
                switch (moveRequest[4].toLowerCase()) {
                    case "pawn" -> promotion = ChessPiece.PieceType.PAWN;
                    case "rook" -> promotion = ChessPiece.PieceType.ROOK;
                    case "knight" -> promotion = ChessPiece.PieceType.KNIGHT;
                    case "bishop" -> promotion = ChessPiece.PieceType.BISHOP;
                    case "queen" -> promotion = ChessPiece.PieceType.QUEEN;
                    default -> {return new ReturnGameResponse(null, "Error: Invalid promotion piece type");}
                }
            }

            int startCol = Integer.parseInt(moveRequest[0]);
            int startRow = Integer.parseInt(moveRequest[1]);
            ChessPosition startPos = new ChessPosition(startRow, startCol);

            int endCol = Integer.parseInt(moveRequest[2]);
            int endRow = Integer.parseInt(moveRequest[3]);
            ChessPosition endPos = new ChessPosition(endRow, endCol);
            ChessMove move = new ChessMove(startPos, endPos, promotion);

            GameData game = gameDAO.makeMove(request.gameID(), auth.username(), move);
            return new ReturnGameResponse(game, "");
        } catch (DataAccessException e) {
            return new ReturnGameResponse(null, e.getMessage());
        }
    }
}
