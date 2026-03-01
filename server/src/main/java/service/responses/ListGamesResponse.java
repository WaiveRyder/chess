package service.responses;

import model.GameData;

import java.util.Collection;

public record ListGamesResponse(Collection<GameData> gamesList, String message) {}
