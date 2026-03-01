package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.requests.AuthRequest;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;
import service.responses.AuthResponse;
import service.responses.GenericResponse;

public class UserServiceTests {
    private UserDAO userDao;
    private AuthDAO authDAO;

    private UserService userService;

    private AuthResponse mockUserAuthData;

    @BeforeEach
    public void setup() {
        userDao = new UserDAO();
        authDAO = new AuthDAO();

        userService = new UserService(userDao, authDAO);

        mockUserAuthData = userService.registerUser(new RegisterRequest("Wally", "passwords", "wally@gmail.com"));
    }

    @Test
    public void registerNewUserNewInfo() {
        RegisterRequest request = new RegisterRequest("John", "password", "john@email.com");

        AuthResponse actual = userService.registerUser(request);

        AuthResponse expected = new AuthResponse("John", actual.authToken(), "");

        Assertions.assertEquals(expected, actual);
        Assertions.assertNotEquals("", actual.authToken());
    }

    @Test
    public void registerNewUserOldInfo() {
        RegisterRequest request = new RegisterRequest("Wally", "passwords", "wally@gmail.com");

        AuthResponse actual = userService.registerUser(request);

        AuthResponse expected = new AuthResponse("", "", "Database already contains username: Wally");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void loginWithGoodInfo() {
        AuthResponse actual = userService.loginUser(new LoginRequest("Wally", "passwords"));

        AuthResponse expected = new AuthResponse("Wally", actual.authToken(), "");

        Assertions.assertEquals(expected, actual);
        Assertions.assertNotEquals("", actual.authToken());
    }

    @Test
    public void loginWithBadInfo() {
        AuthResponse actual = userService.loginUser(new LoginRequest("Wall", "passwords"));

        AuthResponse expected = new AuthResponse("", "", "Database does not contain a user called: Wall");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void logoutUserWithValidToken() {
        GenericResponse actual = userService.logoutUser(new AuthRequest(mockUserAuthData.authToken()));

        GenericResponse expected = new GenericResponse("");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void logoutUserWithInvalidToken() {
        GenericResponse actual = userService.logoutUser(new AuthRequest("ThisIsAnAuthTokenTrustMe"));

        GenericResponse expected = new GenericResponse("Given token is not valid");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void clearDatabase() {
        userService.clear();


    }
}
