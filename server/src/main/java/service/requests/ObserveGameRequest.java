package service.requests;

public record ObserveGameRequest(Integer gameID, String token, boolean leave) {}
