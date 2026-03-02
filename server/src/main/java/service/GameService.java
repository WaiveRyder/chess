package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import service.requests.AuthRequest;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.responses.CreateGameResponse;
import service.responses.GenericResponse;
import service.responses.ListGamesResponse;

public class GameService {
    private AuthDAO authDAO;
    private GameDAO gameDAO;

    public GameService (AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void clear () {
        gameDAO.clear();
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

    public GenericResponse joinGame(JoinGameRequest request) {
        try {
            AuthData auth = authenticate(request.authToken());
            GameData game = gameDAO.joinGame(request.gameID(), auth.username(), request.playerColor());
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
}
