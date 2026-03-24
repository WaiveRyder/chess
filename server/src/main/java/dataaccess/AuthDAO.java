package dataaccess;

import dataaccess.offline.AuthDAOMap;
import model.AuthData;
import model.UserData;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class AuthDAO {
    AuthDAOMap authDAOMap;
    boolean useMap;

    public AuthDAO() {
        useMap = false;
    }

    public AuthDAO(Map<String, AuthData> authMap) {
        authDAOMap = new AuthDAOMap(authMap);
        useMap = true;
    }

    public AuthData createAuth(UserData user) throws DataAccessException {
        if (useMap) {
            return authDAOMap.createAuth(user);
        }
        String token = UUID.randomUUID().toString();
        var statement = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(statement)) {
            pstmt.setString(1, user.username());
            try(ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    statement = "INSERT INTO auth (token, username) VALUES (?, ?)";
                    try (PreparedStatement npstmt = conn.prepareStatement(statement)) {
                        npstmt.setString(1, token);
                        npstmt.setString(2, user.username());
                        npstmt.executeUpdate();
                    }
                } else {
                    throw new DataAccessException("Error: Username not recognized: "
                            + user.username() + ".");
                }
            }
            return new AuthData(token, user.username());
        } catch (SQLException e) {
            throw new DataAccessException("Error: could not connect to the database. " +
                    "Please try again later.. Please try again later.");
        }
    }

    public AuthData getAuthData(String token) throws DataAccessException {
        if (useMap) {
            return authDAOMap.getAuthData(token);
        } else {
            var statement = "SELECT * FROM auth WHERE token = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(statement)) {
                pstmt.setString(1, token);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String username = rs.getString("username");
                        return new AuthData(token, username);
                    }
                    throw new DataAccessException("Error: Authorization token is not valid. Please login again.");
                }
            } catch (SQLException e) {
                throw new DataAccessException("Error: could not connect to the database. Please try again later.");
            }
        }
    }

    public void deleteAuthData(String token) throws DataAccessException {
        if (useMap) {
            authDAOMap.deleteAuthData(token);
        } else {
            var statement = "DELETE FROM auth WHERE token = ?";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(statement)) {
                pstmt.setString(1, token);

                if (pstmt.executeUpdate() == 0) {
                    throw new DataAccessException("Error: Authorization token is not valid. Please login again.");
                }
            } catch (SQLException e) {
                throw new DataAccessException("Error: could not connect to the database. Please try again later.");
            }
        }
    }

    public void clear() throws DataAccessException {
        if (useMap) {
            authDAOMap.clear();
        } else {
            var statement = "DELETE FROM auth";

            try(Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(statement)) {
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new DataAccessException("Error: could not connect to the database. Please try again later.");
            }
        }
    }
}
