package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOTests {
    UserDAO userDAO;
    AuthDAO authDAO;

    AuthData mockUser;

    @BeforeEach
    public void setup() {
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.initTables();

            authDAO = new AuthDAO();
            userDAO = new UserDAO();

            authDAO.clear();
            userDAO.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createUserValidInfo() {
        try {
            UserData actual = userDAO.createUser("John", "password", "email");
            UserData expected = new UserData("John", actual.password(), "email");
            Assertions.assertEquals(expected, actual);
            Assertions.assertNotNull(actual.password());

            var statement = "SELECT * FROM users WHERE username = ?";
            try (var conn = DatabaseManager.getConnection();
                 var pstmt = conn.prepareStatement(statement)) {
                pstmt.setString(1, "John");
                try (ResultSet rs = pstmt.executeQuery()) {
                    Assertions.assertTrue(rs.next());
                    Assertions.assertEquals("John", rs.getString("username"));
                    Assertions.assertEquals(actual.password(), rs.getString("password"));
                    Assertions.assertEquals("email", rs.getString("email"));
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createUserInvalidInfo() {
        try {
            userDAO.createUser("John", "password", "email");
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            userDAO.createUser("John", "password", "different");
            Assertions.fail("Expected an error to be thrown here, user already exists");
        } catch (DataAccessException e) {
            Assertions.assertEquals("Error: Database already contains username: John", e.getMessage());
        }

        var statement = "SELECT * FROM users WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(statement)) {
            pstmt.setString(1, "John");
            try (ResultSet rs = pstmt.executeQuery()) {
                Assertions.assertTrue(rs.next());
                Assertions.assertFalse(rs.next());
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
