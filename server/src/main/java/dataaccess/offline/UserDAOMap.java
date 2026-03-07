package dataaccess.offline;

import dataaccess.DataAccessException;
import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class UserDAOMap {
    private Map<String, UserData> userMap;

    public UserDAOMap(Map<String, UserData> userMap) {
        this.userMap = userMap;
    }

    public UserData createUser(String username, String password, String email) throws DataAccessException {
        if (userMap.containsKey(username)) {
            throw new DataAccessException("Error: Database already contains username: " + username);
        } else {
            UserData newUser = new UserData(username, password, email);
            userMap.put(username, newUser);
            return newUser;
        }
    }

    public UserData getUser(String username) throws DataAccessException {
        UserData user = userMap.get(username);
        if (user == null) {
            throw new DataAccessException("Error: Database does not contain a user called: " + username);
        } else {
            return user;
        }
    }

    public void clear() {
        userMap.clear();
    }
}
