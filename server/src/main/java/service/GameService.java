package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import service.requests.AuthRequest;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;

public class GameService {
    AuthDAO authDAO;
    GameDAO gameDAO;

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

    public RequestAndResponse createGame(CreateGameRequest request) {
        try {
            AuthData auth = authenticate(request.authToken());
            GameData newGame = gameDAO.createGame(request.gameName());
            return new RequestAndResponse().setGameID(newGame.gameID());
        } catch (DataAccessException e) {
            return new RequestAndResponse().setErrorMessage(e.getMessage());
        }
    }

    public RequestAndResponse joinGame(JoinGameRequest request) {
        try {
            AuthData auth = authenticate(request.authToken());
            GameData game = gameDAO.joinGame(request.gameID(), auth.username(), request.playerColor());
            return new RequestAndResponse();
        } catch (DataAccessException e) {
            return new RequestAndResponse().setErrorMessage(e.getMessage());
        }
    }

    public RequestAndResponse listGames(AuthRequest request) {
        try {
            AuthData auth = authenticate(request.authToken());
            return new RequestAndResponse().setGamesList(gameDAO.listGames());
        } catch (DataAccessException e) {
            return new RequestAndResponse().setErrorMessage(e.getMessage());
        }
    }
}
