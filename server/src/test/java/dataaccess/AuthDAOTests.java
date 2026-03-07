package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class AuthDAOTests {
    AuthDAO authDAO;
    UserDAO userDAO;
    GameDAO gameDAO;

    UserData mockUser;

    @BeforeEach
    public void setup() {
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.initTables();

            authDAO = new AuthDAO();
            userDAO = new UserDAO();
            gameDAO = new GameDAO();

            authDAO.clear();
            userDAO.clear();
            gameDAO.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            mockUser = userDAO.createUser("john", "password", "email");
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createAuthValidUserTest() {
        try {
            AuthData actual = authDAO.createAuth(mockUser);
            AuthData expected = new AuthData(actual.authToken(), "john");

            try (Connection conn = DatabaseManager.getConnection()) {
                var statement = "SELECT * FROM auth WHERE token = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(statement)) {
                    pstmt.setString(1, actual.authToken());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        Assertions.assertTrue(rs.next());
                        Assertions.assertEquals("john", rs.getString("username"));
                        Assertions.assertEquals(actual.authToken(), rs.getString("token"));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            Assertions.assertEquals(expected, actual);
            Assertions.assertNotNull(actual.authToken());
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createAuthInvalidUserTest() {
        try {
            authDAO.createAuth(new UserData("Sarah", "password", "email"));
            Assertions.fail("Expected an error to be thrown here, no user in system");

            try (Connection conn = DatabaseManager.getConnection()) {
                var statement = "SELECT * FROM auth WHERE username = Sarah";
                try (PreparedStatement pstmt = conn.prepareStatement(statement)) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        Assertions.assertFalse(rs.next());
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (DataAccessException e) {
            Assertions.assertEquals("Error: Database does not contain a user called: Sarah", e.getMessage());
        }
    }

    @Test
    public void getAuthDataValidTokenTest() {
        try {
            AuthData mockData = authDAO.createAuth(mockUser);
            AuthData actual = authDAO.getAuthData(mockData.authToken());
            AuthData expected = new AuthData(actual.authToken(), mockData.username());
            Assertions.assertEquals(expected, actual);
            Assertions.assertEquals(actual, mockData);
            Assertions.assertNotNull(actual.authToken());

            var statement = "SELECT * FROM auth WHERE token = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(statement)) {
                pstmt.setString(1, actual.authToken());
                try (ResultSet rs = pstmt.executeQuery()) {
                    Assertions.assertTrue(rs.next());
                    Assertions.assertEquals("john", rs.getString("username"));
                    Assertions.assertEquals(actual.authToken(), rs.getString("token"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getAuthDataInvalidTokenTest() {
        try {
            authDAO.getAuthData("ThisIsAValidAuthTokenTrustMe");
            Assertions.fail("Expected an error to be thrown here, no auth in system");

        } catch (DataAccessException e) {
            Assertions.assertEquals("Error: Given token is not valid", e.getMessage());
        }

        var statement = "SELECT * FROM auth WHERE token = 'ThisIsAValidAuthTokenTrustMe'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(statement)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                Assertions.assertFalse(rs.next());
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
