package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GameDAOTests {
    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;

    AuthData mockUser;
    Gson gson;

    @BeforeEach
    public void setup() {
        gson = new Gson();
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.initTables();

            authDAO = new AuthDAO();
            userDAO = new UserDAO();
            gameDAO = new GameDAO();

            authDAO.clear();
            userDAO.clear();
            gameDAO.clear();

            UserData mockData = userDAO.createUser("John", "password", "email");
            mockUser = authDAO.createAuth(mockData);

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createGameValidInfo() {
        ChessGame blankGame = new ChessGame();
        try {
            GameData actual = gameDAO.createGame("New Game");

            GameData expected = new GameData(1, null, null, "New Game", blankGame);
            Assertions.assertEquals(expected, actual);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        var statement = "SELECT * FROM game WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(statement)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                Assertions.assertTrue(rs.next());
                Assertions.assertEquals(1, rs.getInt("gameID"));
                Assertions.assertNull(rs.getString("whiteUsername"));
                Assertions.assertNull(rs.getString("blackUsername"));
                Assertions.assertEquals("New Game", rs.getString("gameName"));
                Assertions.assertEquals(blankGame, gson.fromJson(rs.getString("chessGame"), ChessGame.class));
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createGameInvalidInfo() {
        try {
            gameDAO.createGame(null);
            Assertions.fail("Expected an error to be thrown here, game can't be null");
        } catch (DataAccessException e) {
            Assertions.assertEquals("Error: Game name cannot be null", e.getMessage());
        }

        var statement = "SELECT * FROM game WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(statement)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                Assertions.assertFalse(rs.next());
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void listGamesValidInfo() {
        ChessGame blankGame = new ChessGame();
        try {
            GameData actualFirstGame = gameDAO.createGame("First Game");
            GameData actualSecondGame = gameDAO.createGame("Second Game");

            GameData expectedFirstGame = new GameData(1, null, null, "First Game", blankGame);
            GameData expectedSecondGame = new GameData(2, null, null, "Second Game", blankGame);
            Assertions.assertEquals(expectedFirstGame, actualFirstGame);
            Assertions.assertEquals(expectedSecondGame, actualSecondGame);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        var statement = "SELECT * FROM game";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(statement)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                Assertions.assertTrue(rs.next());
                Assertions.assertEquals(1, rs.getInt("gameID"));
                Assertions.assertNull(rs.getString("whiteUsername"));
                Assertions.assertNull(rs.getString("blackUsername"));
                Assertions.assertEquals("First Game", rs.getString("gameName"));
                Assertions.assertEquals(blankGame, gson.fromJson(rs.getString("chessGame"), ChessGame.class));

                Assertions.assertTrue(rs.next());
                Assertions.assertEquals(2, rs.getInt("gameID"));
                Assertions.assertNull(rs.getString("whiteUsername"));
                Assertions.assertNull(rs.getString("blackUsername"));
                Assertions.assertEquals("Second Game", rs.getString("gameName"));
                Assertions.assertEquals(blankGame, gson.fromJson(rs.getString("chessGame"), ChessGame.class));

                Assertions.assertFalse(rs.next());
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
