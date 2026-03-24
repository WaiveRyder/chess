package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.requests.AuthRequest;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.responses.CreateGameResponse;
import service.responses.GenericResponse;
import service.responses.ListGamesResponse;
import service.responses.ReturnGameResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class GameServiceTests {

    private Map<String, AuthData> authMap;
    Map<Integer, GameData> gameMap;

    private GameService gameService;

    private AuthData mockUser;
    private AuthData mockUserN;

    private ChessGame game;

    @BeforeEach
    public void setup() {
        authMap = new HashMap<>();
        AuthDAO authDAO = new AuthDAO(authMap);

        gameMap = new HashMap<>();
        GameDAO gameDAO = new GameDAO(gameMap);

        gameService = new GameService(authDAO, gameDAO);

        try {
            mockUser = authDAO.createAuth(new UserData("john", "password", "email"));
            mockUserN = authDAO.createAuth(new UserData("jim", "pass", "email"));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        game = new ChessGame();

        gameService.createGame(new CreateGameRequest(mockUser.authToken(), "First Game"));

        gameService.joinGame(new JoinGameRequest(mockUser.authToken(), ChessGame.TeamColor.BLACK, 1));
    }

    @Test
    public void createGameValidToken() {
        CreateGameResponse actual = gameService.createGame(
                new CreateGameRequest(mockUser.authToken(), "Hello")
        );

        CreateGameResponse expected = new CreateGameResponse(2, "");

        GameData expectedGame = new GameData(2, null, null, "Hello", game);

        GameData actualGame = gameMap.get(2);

        Assertions.assertEquals(expected, actual);
        Assertions.assertEquals(expectedGame, actualGame);
    }

    @Test
    public void createGameInvalidToken() {
        CreateGameResponse actual = gameService.createGame(
                new CreateGameRequest("ThisIsAValidAuthTokenTrustMe", "Hello")
        );

        CreateGameResponse expected = new CreateGameResponse(null, "Error: Given token is not valid");

        Assertions.assertEquals(expected, actual);
        Assertions.assertNull(gameMap.get(2));
    }

    @Test
    public void joinGameValidId() {
        ReturnGameResponse actual = gameService.joinGame(
                new JoinGameRequest(mockUser.authToken(), ChessGame.TeamColor.WHITE, 1)
        );

        GameData gameData = new GameData(1, mockUser.username(), "john", "First Game", game);
        ReturnGameResponse expected = new ReturnGameResponse(gameData, "");

        GameData actualGame = gameMap.get(1);

        Assertions.assertEquals(expected, actual);
        Assertions.assertEquals(gameData, actualGame);
    }

    @Test
    public void joinGameColorFull() {
        ReturnGameResponse actual = gameService.joinGame(
                new JoinGameRequest(mockUserN.authToken(), ChessGame.TeamColor.BLACK, 1)
        );


        ReturnGameResponse expected = new ReturnGameResponse(null,"Error: Cannot join game because BLACK is taken. Game id: 1");

        GameData expectedGame = new GameData(1, null, "john", "First Game", game);
        GameData actualGame = gameMap.get(1);

        Assertions.assertEquals(expected, actual);
        Assertions.assertEquals(expectedGame, actualGame);
    }

    @Test
    public void listGamesValidId() {
        ListGamesResponse actual = gameService.listGames(new AuthRequest(mockUser.authToken()));

        ListGamesResponse expected;

        Collection<GameData> gamesList = new Vector<>();
        gamesList.add(new GameData(
                1,
                null,
                "john",
                "First Game",
                new ChessGame()
            ));
        expected = new ListGamesResponse(gamesList, "");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void listGamesInvalidId() {
        ListGamesResponse actual = gameService.listGames(new AuthRequest("ThisIsAValidTokenTrustMe"));

        ListGamesResponse expected = new ListGamesResponse(null, "Error: Given token is not valid");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void clearDatabase() {
        gameService.clear();

        System.out.println(gameMap.values());

        Assertions.assertTrue(gameService.listGames(new AuthRequest(mockUser.authToken())).games().isEmpty());
        Assertions.assertTrue(gameMap.isEmpty());
    }

}
