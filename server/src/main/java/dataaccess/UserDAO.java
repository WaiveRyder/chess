package dataaccess;

import dataaccess.offline.UserDAOMap;
import model.UserData;

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
            return null;
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
