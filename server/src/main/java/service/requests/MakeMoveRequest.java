package service.requests;

public record MakeMoveRequest(String authToken, Integer gameID, String move) {}
