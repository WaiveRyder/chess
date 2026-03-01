package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import service.requests.AuthRequest;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;
import service.responses.AuthResponse;
import service.responses.GenericResponse;

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

    public AuthResponse registerUser(RegisterRequest request) {
        try {
            AuthData newAuth = authDAO.createAuth(
                    userDAO.createUser(
                            request.username(),
                            request.password(),
                            request.email())
            );
            return new AuthResponse(newAuth.username(), newAuth.authToken(), "");
        } catch (DataAccessException e) {
            return new AuthResponse("", "", e.getMessage());
        }
    }

    public AuthResponse loginUser(LoginRequest request) {
        try {
            UserData user = userDAO.getUser(request.username());
            if (user.password().equals(request.password())) {
                AuthData newAuth = authDAO.createAuth(user);
                return new AuthResponse(newAuth.username(), newAuth.authToken(), "");
            } else {
                return new AuthResponse("", "", "Password is incorrect");
            }
        } catch (DataAccessException e) {
            return new AuthResponse("", "", e.getMessage());
        }
    }

    public GenericResponse logoutUser(AuthRequest request) {
        try {
            authDAO.deleteAuthData(request.authToken());
            return new GenericResponse("");
        } catch (DataAccessException e) {
            return new GenericResponse(e.getMessage());
        }
    }
}
