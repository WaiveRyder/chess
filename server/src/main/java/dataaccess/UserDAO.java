package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private Map<String, UserData> usersMap;

    public UserDAO() {
        usersMap = new HashMap<String, UserData>();
    }

    public void createUser(String username, String password, String email) throws DataAccessException {
        if (usersMap.containsKey(username)) {
            throw new DataAccessException("Database already contains username: " + username);
        } else {
            usersMap.put(username, new UserData(username, password, email));
        }
    }

    public UserData getUser(String username) throws DataAccessException {
        UserData user = usersMap.get(username);
        if (user == null) {
            throw new DataAccessException("Database does not contain a user called: " + username);
        } else {
            return user;
        }
    }

    public void clear() {
        usersMap = new HashMap<String, UserData>();
    }
}
