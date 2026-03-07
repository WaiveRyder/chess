package dataaccess.offline;

import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthDAOMap {
    private Map<String, AuthData> authMap;

    public AuthDAOMap() {
        authMap = new HashMap<>();
    }

    public AuthDAOMap(Map<String, AuthData> authMap) {
        this.authMap = authMap;
    }

    public AuthData createAuth(UserData user) {
        AuthData newAuthData = new AuthData(UUID.randomUUID().toString(), user.username());
        authMap.put(newAuthData.authToken(), newAuthData);
        return newAuthData;
    }

    public AuthData getAuthData(String token) throws DataAccessException {
        AuthData auth = authMap.get(token);
        if(auth == null) {
            throw new DataAccessException("Error: Given token is not valid");
        } else {
            return auth;
        }
    }

    public void deleteAuthData(String token) throws DataAccessException {
        AuthData auth = authMap.get(token);
        if(auth == null) {
            throw new DataAccessException("Error: Given token is not valid");
        } else {
            authMap.remove(token);
        }
    }

    public void clear() {
        authMap.clear();
    }
}
