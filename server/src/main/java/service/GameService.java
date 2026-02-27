package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

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

    public RequestAndResponse createGame(RequestAndResponse request) {
        try {
            AuthData auth = authenticate(request.getAuthToken());
            GameData newGame = gameDAO.createGame(request.getGameName());
            return new RequestAndResponse().setGameID(newGame.gameID());
        } catch (DataAccessException e) {
            return new RequestAndResponse().setErrorMessage(e.getMessage());
        }
    }

    public RequestAndResponse joinGame(RequestAndResponse request) {
        try {
            AuthData auth = authenticate(request.getAuthToken());
            GameData game = gameDAO.joinGame(request.getGameID(), auth.username(), request.getPlayerColor());
            return new RequestAndResponse();
        } catch (DataAccessException e) {
            return new RequestAndResponse().setErrorMessage(e.getMessage());
        }
    }

    public RequestAndResponse listGames(RequestAndResponse request) {
        try {
            AuthData auth = authenticate(request.getAuthToken());
            return new RequestAndResponse().setGamesList(gameDAO.listGames());
        } catch (DataAccessException e) {
            return new RequestAndResponse().setErrorMessage(e.getMessage());
        }
    }
}
