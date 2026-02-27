package service;

import model.GameData;

import java.util.Collection;

public class RequestAndResponse {
    private String username;
    private String password;
    private String email;
    private String authToken;
    private String gameName;
    private String playerColor;
    private int gameID;
    private Collection<GameData> gamesList;

    private String errorMessage;

    public String getUsername() {
        return username;
    }

    public RequestAndResponse setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RequestAndResponse setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public RequestAndResponse setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getAuthToken() {
        return authToken;
    }

    public RequestAndResponse setAuthToken(String authToken) {
        this.authToken = authToken;
        return this;
    }

    public String getGameName() {
        return gameName;
    }

    public RequestAndResponse setGameName(String gameName) {
        this.gameName = gameName;
        return this;
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public RequestAndResponse setPlayerColor(String playerColor) {
        this.playerColor = playerColor;
        return this;
    }

    public int getGameID() {
        return gameID;
    }

    public RequestAndResponse setGameID(int gameID) {
        this.gameID = gameID;
        return this;
    }

    public Collection<GameData> getGamesList() {
        return gamesList;
    }

    public RequestAndResponse setGamesList(Collection<GameData> gamesList) {
        this.gamesList = gamesList;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public RequestAndResponse setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }
}
