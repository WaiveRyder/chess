package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

public class UserService {
    UserDAO userDAO;
    AuthDAO authDAO;

    public UserService (UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public void clear() {
        userDAO.clear();
        authDAO.clear();
    }

    public RequestAndResponse registerUser(RequestAndResponse request) {
        try {
            AuthData newAuth = authDAO.createAuth(
                    userDAO.createUser(
                            request.getUsername(),
                            request.getPassword(),
                            request.getEmail())
            );
            return new RequestAndResponse().setUsername(newAuth.username()).setAuthToken(newAuth.authToken());
        } catch (DataAccessException e) {
            return new RequestAndResponse().setErrorMessage(e.getMessage());
        }
    }

    public RequestAndResponse loginUser(RequestAndResponse request) {
        try {
            UserData user = userDAO.getUser(request.getUsername());
            if (user.password().equals(request.getPassword())) {
                AuthData newAuth = authDAO.createAuth(user);
                return new RequestAndResponse().setUsername(newAuth.username()).setAuthToken(newAuth.authToken());
            } else {
                throw new DataAccessException("Password is incorrect");
            }
        } catch (DataAccessException e) {
            return new RequestAndResponse().setErrorMessage(e.getMessage());
        }
    }

    public RequestAndResponse logoutUser(RequestAndResponse request) {
        try {
            authDAO.deleteAuthData(request.getAuthToken());
            return new RequestAndResponse();
        } catch (DataAccessException e) {
            return new RequestAndResponse().setErrorMessage(e.getMessage());
        }
    }
}
