import client.ServerFacade;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


public class ServerFacadeTests {

    private static Server server;

    ServerFacade serverFacade;

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
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

    }

}
