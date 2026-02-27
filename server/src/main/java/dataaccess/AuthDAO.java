package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthDAO {
    private Map<String, AuthData> authMap;

    public AuthDAO() {
        authMap = new HashMap<>();
    }

    public AuthData createAuth(UserData user) {
        return new AuthData(UUID.randomUUID().toString(), user.username(), "");
    }

    public AuthData getAuthData(String token) throws DataAccessException {
        AuthData auth = authMap.get(token);
        if(auth == null) {
            throw new DataAccessException("Given token is not valid");
        } else {
            return auth;
        }
    }

    public void deleteAuthData(String token) throws DataAccessException {
        AuthData auth = authMap.get(token);
        if(auth == null) {
            throw new DataAccessException("Given token is not valid");
        } else {
            authMap.remove(token);
        }
    }

    public void clear() {
        authMap = new HashMap<>();
    }
}
