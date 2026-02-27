package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.AuthData;

import javax.xml.crypto.Data;

public class AuthService {
    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;

    public AuthService (UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void clear () {
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }

    public AuthData authenticate(String token) {
        try {
            return authDAO.getAuthData(token);
        } catch (DataAccessException e) {
            return null;
        }
    }
}
