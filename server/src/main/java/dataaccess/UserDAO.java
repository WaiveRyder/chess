package dataaccess;

import dataaccess.offline.UserDAOMap;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.mindrot.jbcrypt.BCrypt;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class UserDAO {
    DatabaseManager databaseManager;
    UserDAOMap userDAOMap;
    boolean useMap;

    public UserDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        useMap = false;
    }

    public UserDAO(Map<String, UserData> userMap) {
        userDAOMap = new UserDAOMap(userMap);
        useMap = true;
    }

    public UserData createUser(String username, String password, String email) throws DataAccessException {
        if (useMap) {
            return userDAOMap.createUser(username, password, email);
        } else {
            try (Connection conn = DatabaseManager.getConnection()) {
                var statement = "SELECT 1 FROM users WHERE username = ?";

                try (PreparedStatement check = conn.prepareStatement(statement)) {
                    check.setString(1, username);

                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) {
                            throw new DataAccessException("Error: Database already contains username: " + username);
                        }
                        statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
                        try (PreparedStatement insert = conn.prepareStatement(statement) ) {
                            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                            insert.setString(1, username);
                            insert.setString(2, hashedPassword);
                            insert.setString(3, email);
                            insert.executeUpdate();
                            return new UserData(username, hashedPassword, email);

                        }
                    }
                }
            } catch (SQLException e) {
                throw new DataAccessException("could not connect to database", e);
            }
        }
    }

    public UserData getUser(String username) throws DataAccessException {
        if (useMap) {
            return userDAOMap.getUser(username);
        } else {
            return null;
        }
    }

    public void clear() {
        if (useMap) {
            userDAOMap.clear();
        }
    }
}
