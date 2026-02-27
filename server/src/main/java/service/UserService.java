package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

public class UserService {
    UserDAO userDAO;
    AuthDAO authDAO;

    public UserService (UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData registerUser(UserData user) {
        try {
            return authDAO.createAuth(userDAO.createUser(user.username(), user.password(), user.email()));
        } catch (DataAccessException e) {
            return new AuthData("", "", e.getMessage());
        }
    }

    public AuthData loginUser(UserData user) {
        try {
            return authDAO.createAuth(userDAO.getUser(user.username()));
        } catch (DataAccessException e) {
            return new AuthData("", "", e.getMessage());
        }
    }

    public AuthData logoutUser(AuthData user) {
        try {
            authDAO.deleteAuthData(user.authToken());
            return new AuthData(null, null, null);
        } catch (DataAccessException e) {
            return new AuthData(null, null, e.getMessage());
        }
    }
}
