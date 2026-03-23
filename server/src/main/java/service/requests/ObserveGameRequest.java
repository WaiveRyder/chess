package service.requests;

public record ObserveGameRequest(int gameID, String token, boolean leave) {}
