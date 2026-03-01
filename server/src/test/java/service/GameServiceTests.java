package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
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

import java.util.Collection;
import java.util.Vector;

public class GameServiceTests {

    private GameService gameService;

    private AuthData mockUser;

    @BeforeEach
    public void setup() {
        AuthDAO authDAO = new AuthDAO();
        GameDAO gameDAO = new GameDAO();

        gameService = new GameService(authDAO, gameDAO);

        mockUser = authDAO.createAuth(new UserData("john", "password", "email"));

        gameService.createGame(new CreateGameRequest(mockUser.authToken(), "First Game"));

        gameService.joinGame(new JoinGameRequest(mockUser.authToken(), ChessGame.TeamColor.BLACK, 0));
    }

    @Test
    public void createGameValidToken() {
        CreateGameResponse actual = gameService.createGame(
                new CreateGameRequest(mockUser.authToken(), "Hello")
        );

        CreateGameResponse expected = new CreateGameResponse(1, "");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void createGameInvalidToken() {
        CreateGameResponse actual = gameService.createGame(
                new CreateGameRequest("ThisIsAValidAuthTokenTrustMe", "Hello")
        );

        CreateGameResponse expected = new CreateGameResponse(0, "Given token is not valid");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void joinGameValidId() {
        GenericResponse actual = gameService.joinGame(
                new JoinGameRequest(mockUser.authToken(), ChessGame.TeamColor.WHITE, 0)
        );

        GenericResponse expected = new GenericResponse("");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void joinGameColorFull() {
        GenericResponse actual = gameService.joinGame(
                new JoinGameRequest(mockUser.authToken(), ChessGame.TeamColor.BLACK, 0)
        );


        GenericResponse expected = new GenericResponse("Cannot join game because BLACK is taken. Game id: 0");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void listGamesValidId() {
        ListGamesResponse actual = gameService.listGames(new AuthRequest(mockUser.authToken()));

        ListGamesResponse expected;

        Collection<GameData> gamesList = new Vector<>();
        gamesList.add(new GameData(
                0,
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

        ListGamesResponse expected = new ListGamesResponse(null, "Given token is not valid");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void clearDatabase() {
        gameService.clear();

        ListGamesResponse gamesList = gameService.listGames(new AuthRequest(mockUser.authToken()));

        Assertions.assertTrue(gamesList.gamesList().isEmpty());
    }

}
