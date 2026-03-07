package dataaccess;

import dataaccess.offline.AuthDAOMap;
import model.AuthData;
import model.UserData;

import java.util.Map;

public class AuthDAO {
    DatabaseManager database;
    AuthDAOMap authDAOMap;
    boolean useMap;

    public AuthDAO(DatabaseManager database) {
        this.database = database;
        useMap = false;
    }

    public AuthDAO(Map<String, AuthData> authMap) {
        authDAOMap = new AuthDAOMap(authMap);
        useMap = true;
    }

    public AuthData createAuth(UserData user) {
        if (useMap) {
            return authDAOMap.createAuth(user);
        }
    }

    public AuthData getAuthData(String token) throws DataAccessException {
        if (useMap) {
            return authDAOMap.getAuthData(token);
        }
    }

    public void deleteAuthData(String token) throws DataAccessException {
        if (useMap) {
            authDAOMap.deleteAuthData(token);
        }
    }

    public void clear() {
        if (useMap) {
            authDAOMap.clear();
        }
    }
}
