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

            ChessMove move = request.move();

            GameData game = gameDAO.makeMove(request.gameID(), auth.username(), move);
            return new ReturnGameResponse(game, "");
        } catch (DataAccessException e) {
            return new ReturnGameResponse(null, e.getMessage());
        }
    }

    public GenericResponse resignGame(ResignGameRequest request) {
        try {
            String username = authDAO.getAuthData(request.authToken()).username();
            gameDAO.resign(request.gameID(), username);
            return new GenericResponse("");
        } catch (DataAccessException e) {
            return new GenericResponse(e.getMessage());
        }
    }
}
