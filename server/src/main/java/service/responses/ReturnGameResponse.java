package service.responses;

import model.GameData;

public record ReturnGameResponse(GameData gameData, String message) {}
