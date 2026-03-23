package service.responses;

import model.GameData;

public record ReturnGameResponse(GameData game, String message) {}
