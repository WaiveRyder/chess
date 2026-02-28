package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import service.requests.AuthRequest;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;

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

    public RequestAndResponse registerUser(RegisterRequest request) {
        try {
            AuthData newAuth = authDAO.createAuth(
                    userDAO.createUser(
                            request.username(),
                            request.password(),
                            request.email())
            );
            return new RequestAndResponse().setUsername(newAuth.username()).setAuthToken(newAuth.authToken());
        } catch (DataAccessException e) {
            return new RequestAndResponse().setErrorMessage(e.getMessage());
        }
    }

    public RequestAndResponse loginUser(LoginRequest request) {
        try {
            UserData user = userDAO.getUser(request.username());
            if (user.password().equals(request.password())) {
                AuthData newAuth = authDAO.createAuth(user);
                return new RequestAndResponse().setUsername(newAuth.username()).setAuthToken(newAuth.authToken());
            } else {
                throw new DataAccessException("Password is incorrect");
            }
        } catch (DataAccessException e) {
            return new RequestAndResponse().setErrorMessage(e.getMessage());
        }
    }

    public RequestAndResponse logoutUser(AuthRequest request) {
        try {
            authDAO.deleteAuthData(request.authToken());
            return new RequestAndResponse();
        } catch (DataAccessException e) {
            return new RequestAndResponse().setErrorMessage(e.getMessage());
        }
    }
}
