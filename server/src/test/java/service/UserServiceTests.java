package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.requests.AuthRequest;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;
import service.responses.AuthResponse;
import service.responses.GenericResponse;

import java.util.HashMap;
import java.util.Map;

public class UserServiceTests {
    private Map<String, AuthData> authMap;
    private Map<String, UserData> userMap;

    private UserService userService;

    private AuthResponse mockUserAuthData;

    @BeforeEach
    public void setup() {
        authMap = new HashMap<>();
        userMap = new HashMap<>();

        UserDAO userDao = new UserDAO(userMap);
        AuthDAO authDAO = new AuthDAO(authMap);

        userService = new UserService(userDao, authDAO);

        mockUserAuthData = userService.registerUser(new RegisterRequest("Wally", "passwords", "wally@gmail.com"));
    }

    @Test
    public void registerNewUserNewInfo() {
        RegisterRequest request = new RegisterRequest("John", "password", "john@email.com");

        AuthResponse actual = userService.registerUser(request);

        AuthResponse expected = new AuthResponse("John", actual.authToken(), "");

        UserData expectedData = new UserData("John", "password", "john@email.com");
        UserData actualData = userMap.get("John");

        Assertions.assertEquals(expected, actual);
        Assertions.assertNotEquals("", actual.authToken());
        Assertions.assertNotNull(authMap.get(actual.authToken()));
        Assertions.assertEquals(expectedData, actualData);
    }

    @Test
    public void registerNewUserOldInfo() {
        RegisterRequest request = new RegisterRequest("Wally", "wowza", "wally@hotmail.com");

        AuthResponse actual = userService.registerUser(request);

        AuthResponse expected = new AuthResponse(null, null, "Error: Database already contains username: Wally");

        int expectedLength = 1;
        int actualLength = userMap.size();

        UserData expectedData = new UserData("Wally", "passwords", "wally@gmail.com");
        UserData acutalData = userMap.get("Wally");

        int expectedAuthLength = 1;
        int actualAuthLength = authMap.size();

        Assertions.assertEquals(expected, actual);
        Assertions.assertEquals(expectedLength, actualLength);
        Assertions.assertEquals(expectedData, acutalData);
        Assertions.assertEquals(expectedAuthLength, actualAuthLength);
    }

    @Test
    public void loginWithGoodInfo() {
        AuthResponse actual = userService.loginUser(new LoginRequest("Wally", "passwords"));

        AuthResponse expected = new AuthResponse("Wally", actual.authToken(), "");

        AuthData expectedData = new AuthData(actual.authToken(), "Wally");
        AuthData actualData = authMap.get(expected.authToken());

        Assertions.assertEquals(expected, actual);
        Assertions.assertNotEquals("", actual.authToken());
        Assertions.assertEquals(expectedData, actualData);
    }

    @Test
    public void loginWithBadInfo() {
        AuthResponse actual = userService.loginUser(new LoginRequest("Wall", "passwords"));

        AuthResponse expected = new AuthResponse(null, null, "Error: Database does not contain a user called: Wall");

        int expectedLength = 1;
        int actualLength = authMap.size();

        Assertions.assertEquals(expected, actual);
        Assertions.assertEquals(expectedLength, actualLength);
    }

    @Test
    public void logoutUserWithValidToken() {
        GenericResponse actual = userService.logoutUser(new AuthRequest(mockUserAuthData.authToken()));

        GenericResponse expected = new GenericResponse("");

        Assertions.assertEquals(expected, actual);
        Assertions.assertNull(authMap.get(mockUserAuthData.authToken()));
    }

    @Test
    public void logoutUserWithInvalidToken() {
        GenericResponse actual = userService.logoutUser(new AuthRequest("ThisIsAnAuthTokenTrustMe"));

        GenericResponse expected = new GenericResponse("Error: Given token is not valid");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void clearDatabase() {
        userService.clear();

        Assertions.assertTrue(authMap.isEmpty());
        Assertions.assertTrue(userMap.isEmpty());
    }
}
