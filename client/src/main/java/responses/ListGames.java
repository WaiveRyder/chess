package responses;

import model.GameData;

import java.util.Collection;

public record ListGames(Collection<GameData> games) {}
