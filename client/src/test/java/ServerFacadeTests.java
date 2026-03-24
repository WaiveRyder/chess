import client.ServerFacade;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


public class ServerFacadeTests {

    private static Server server;
    private static String game;

    ServerFacade serverFacade;

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        game = setGameString();
    }

    @BeforeEach
    public void setup() {
        serverFacade = new ServerFacade(8080);
        serverFacade.clear();

        serverFacade.request("register", "user", "user", "user");
        serverFacade.request("logout");

        System.setOut(new PrintStream(outputStreamCaptor, true, StandardCharsets.UTF_8));
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }


    @Test
    public void testHelp() {
        String exptected = "Commands:" + System.lineSeparator()
                + "- login <username> <password>: Login with username and password" + System.lineSeparator()
                + "- register <username> <password> <email>: Register with username, password, and email"
                + System.lineSeparator()
                + "- quit: Exit the application" + System.lineSeparator()
                + "- help: Lists all commands" + System.lineSeparator();
        serverFacade.request("help");

        Assertions.assertEquals(exptected, outputStreamCaptor.toString());
    }

    @Test
    public void testHelpInvalid() {
        String expected = "\u001B[38;5;160mError: Usage: help\u001B[39m" + System.lineSeparator()
                + "Type 'help' for a list of commands." + System.lineSeparator();
        serverFacade.request("help", "login");

        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void register() {
        String expected = "Successfully registered! Welcome testuser!" + System.lineSeparator();
        serverFacade.request("register", "testuser", "testuser", "testuser");

        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void registerDuplicate() {
        String expected = "\u001B[38;5;160mRegister failed due to Error: Username already in use: user\u001B[39m"
                + System.lineSeparator() + "Type 'help' for a list of commands." + System.lineSeparator();
        serverFacade.request("register", "user", "user", "user");

        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void list() {
        String expected = "Games:" + System.lineSeparator();

        serverFacade.request("login", "user", "user");
        outputStreamCaptor.reset();

        serverFacade.request("list");

        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void listInvalid() {
        String expected = "\u001B[38;5;160mError: Usage: list\u001B[39m" + System.lineSeparator()
                + "Type 'help' for a list of commands." + System.lineSeparator();

        serverFacade.request("login", "user", "user");
        outputStreamCaptor.reset();

        serverFacade.request("list", "extra");
        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void join() {
        String expected = "Successfully joined game with ID: 1 as WHITE" + System.lineSeparator()
                + game;

        serverFacade.request("login", "user", "user");
        serverFacade.request("create", "testgame");
        serverFacade.request("list");
        outputStreamCaptor.reset();

        serverFacade.request("join", "1", "WHITE");

        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void joinInvalid() {
        String expected = "\u001B[38;5;160mError: You must list games before" +
                " trying to join!\u001B[39m" + System.lineSeparator()
                + "Type 'help' for a list of commands." + System.lineSeparator();

        serverFacade.request("login", "user", "user");
        serverFacade.request("create", "testgame");
        outputStreamCaptor.reset();

        serverFacade.request("join", "1", "WHITE");

        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void exit() {
        String expected = "Exited game and returned to menu." + System.lineSeparator();

        serverFacade.request("login", "user", "user");
        serverFacade.request("create", "testgame");
        serverFacade.request("list");
        serverFacade.request("join", "1", "WHITE");
        outputStreamCaptor.reset();

        serverFacade.request("exit");

        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void exitInvalid() {
        String expected = "\u001B[38;5;160mError: You cannot exit a game while not playing one, " +
                "please join a game before trying to exit\u001B[39m" + System.lineSeparator()
                + "Type 'help' for a list of commands." + System.lineSeparator();

        serverFacade.request("login", "user", "user");
        serverFacade.request("create", "testgame");
        serverFacade.request("list");
        outputStreamCaptor.reset();

        serverFacade.request("exit");

        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void create() {
        String expected = "Successfully created game: testgame" + System.lineSeparator();

        serverFacade.request("login", "user", "user");
        outputStreamCaptor.reset();

        serverFacade.request("create", "testgame");

        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void createInvalid() {
        String expected = "\u001B[38;5;160mError: Usage: create <game_name>" +
                " (no spaces allowed in name)\u001B[39m" + System.lineSeparator()
                + "Type 'help' for a list of commands." + System.lineSeparator();

        serverFacade.request("login", "user", "user");
        outputStreamCaptor.reset();

        serverFacade.request("create", "a", "game");

        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void observe() {
        String expected = "Now observing game with ID: 1" + System.lineSeparator() + game;

        serverFacade.request("login", "user", "user");
        serverFacade.request("create", "testgame");
        serverFacade.request("list");
        outputStreamCaptor.reset();

        serverFacade.request("observe", "1");

        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void observeInvalid() {
        String expected = "\u001B[38;5;160mError: You must list games before" +
                " trying to observe!\u001B[39m" + System.lineSeparator()
                + "Type 'help' for a list of commands." + System.lineSeparator();

        serverFacade.request("login", "user", "user");
        serverFacade.request("create", "testgame");
        outputStreamCaptor.reset();

        serverFacade.request("observe", "1");

        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void logout() {
        String expected = "Successfully logged out." + System.lineSeparator();

        serverFacade.request("login", "user", "user");
        outputStreamCaptor.reset();
        serverFacade.request("logout");
        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test public void logoutInvalid() {
        String expected = "\u001B[38;5;160mError: You must be logged in to logout\u001B[39m" + System.lineSeparator()
                + "Type 'help' for a list of commands." + System.lineSeparator();

        serverFacade.request("logout");
        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void leave() {
        String expected = "Stopped observing game." + System.lineSeparator();

        serverFacade.request("login", "user", "user");
        serverFacade.request("create", "testgame");
        serverFacade.request("list");
        serverFacade.request("observe", "1");
        outputStreamCaptor.reset();
        serverFacade.request("leave");
        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void leaveInvalid() {
        String expected = "\u001B[38;5;160mError: You must be observing a game to leave a game\u001B[39m"
                + System.lineSeparator()
                + "Type 'help' for a list of commands." + System.lineSeparator();

        serverFacade.request("login", "user", "user");
        outputStreamCaptor.reset();
        serverFacade.request("leave");
        Assertions.assertEquals(expected, outputStreamCaptor.toString());
    }



    private static String setGameString() {
        String game = "[48;5;242m[38;5;0m   [49m[39m[48;5;242m[38;5;0m a [49m[39m[48;5;242m" +
                "[38;5;0m b [49m[39m[48;5;242m[38;5;0m c [49m[39m[48;5;242m[38;5;0m d" +
                " [49m[39m[48;5;242m[38;5;0m e [49m[39m[48;5;242m[38;5;0m f [49m[39m" +
                "[48;5;242m[38;5;0m g [49m[39m[48;5;242m[38;5;0m h [49m[39m[48;5;242m" +
                "[38;5;0m   [49m[39m" + System.lineSeparator()
                + "[48;5;242m[38;5;0m 8 [49m[39m[48;5;12m [38;5;0mR[39m [49m[48;5;22m " +
                "[38;5;0mN[39m [49m[48;5;12m [38;5;0mB[39m [49m[48;5;22m [38;5;0mQ[39" +
                "m [49m[48;5;12m [38;5;0mK[39m [49m[48;5;22m [38;5;0mB[39m [49m[48;5;" +
                "12m [38;5;0mN[39m [49m[48;5;22m [38;5;0mR[39m [49m[48;5;242m[38;5;0m 8 " +
                "[49m[39m" + System.lineSeparator()
                + "[48;5;242m[38;5;0m 7 [49m[39m[48;5;22m [38;5;0mP[39m [49m[48;5;12m " +
                "[38;5;0mP[39m [49m[48;5;22m [38;5;0mP[39m [49m[48;5;12m [38;5;0mP[39m " +
                "[49m[48;5;22m [38;5;0mP[39m [49m[48;5;12m [38;5;0mP[39m [49m[48;5;22" +
                "m [38;5;0mP[39m [49m[48;5;12m [38;5;0mP[39m [49m[48;5;242m[38;5;0m 7 " +
                "[49m[39m" + System.lineSeparator()
                + "[48;5;242m[38;5;0m 6 [49m[39m[48;5;12m   [49m[48;5;22m   [49m[48;5;" +
                "12m   [49m[48;5;22m   [49m[48;5;12m   [49m[48;5;22m   [49m[48;5;12m   " +
                "[49m[48;5;22m   [49m[48;5;242m[38;5;0m 6 [49m[39m" + System.lineSeparator()
                + "[48;5;242m[38;5;0m 5 [49m[39m[48;5;22m   [49m[48;5;12m   [49m[48;5;2" +
                "2m   [49m[48;5;12m   [49m[48;5;22m   [49m[48;5;12m   [49m[48;5;22m   " +
                "[49m[48;5;12m   [49m[48;5;242m[38;5;0m 5 [49m[39m" + System.lineSeparator()
                + "[48;5;242m[38;5;0m 4 [49m[39m[48;5;12m   [49m[48;5;22m   [49m[48;5;1" +
                "2m   [49m[48;5;22m   [49m[48;5;12m   [49m[48;5;22m   [49m[48;5;12m   [" +
                "49m[48;5;22m   [49m[48;5;242m[38;5;0m 4 [49m[39m" + System.lineSeparator()
                + "[48;5;242m[38;5;0m 3 [49m[39m[48;5;22m   [49m[48;5;12m   [49m[48;5;2" +
                "2m   [49m[48;5;12m   [49m[48;5;22m   [49m[48;5;12m   [49m[48;5;22m   [4" +
                "9m[48;5;12m   [49m[48;5;242m[38;5;0m 3 [49m[39m" + System.lineSeparator()
                + "[48;5;242m[38;5;0m 2 [49m[39m[48;5;12m [38;5;15mP[39m [49m[48;5;22m " +
                "[38;5;15mP[39m [49m[48;5;12m [38;5;15mP[39m [49m[48;5;22m [38;5;15mP" +
                "[39m [49m[48;5;12m [38;5;15mP[39m [49m[48;5;22m [38;5;15mP[39m [49m" +
                "[48;5;12m [38;5;15mP[39m [49m[48;5;22m [38;5;15mP[39m [49m[48;5;242m" +
                "[38;5;0m 2 [49m[39m" + System.lineSeparator()
                + "[48;5;242m[38;5;0m 1 [49m[39m[48;5;22m [38;5;15mR[39m [49m[48;5;12m " +
                "[38;5;15mN[39m [49m[48;5;22m [38;5;15mB[39m [49m[48;5;12m [38;5;15mQ[3" +
                "9m [49m[48;5;22m [38;5;15mK[39m [49m[48;5;12m [38;5;15mB[39m [49m[" +
                "48;5;22m [38;5;15mN[39m [49m[48;5;12m [38;5;15mR[39m [49m[48;5;242m" +
                "[38;5;0m 1 [49m[39m" + System.lineSeparator()
                + "[48;5;242m[38;5;0m   [49m[39m[48;5;242m[38;5;0m a [49m[39m[48;5;242m" +
                "[38;5;0m b [49m[39m[48;5;242m[38;5;0m c [49m[39m[48;5;242m[38;5;0m d " +
                "[49m[39m[48;5;242m[38;5;0m e [49m[39m[48;5;242m[38;5;0m f [49m[39m[4" +
                "8;5;242m[38;5;0m g [49m[39m[48;5;242m[38;5;0m h [49m[39m[48;5;242m[38;" +
                "5;0m   [49m[39m" + System.lineSeparator();
        return game;
    }
}
