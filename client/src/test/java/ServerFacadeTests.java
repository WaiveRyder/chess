import client.ServerFacade;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;


public class ServerFacadeTests {

    private static Server server;

    ServerFacade serverFacade;

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @BeforeEach
    public void setup() {
        serverFacade = new ServerFacade(0);

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
        String exptected = """
                Commands:
                - login <username> <password>: Login with username and password
                - register <username> <password> <email>: Register with username, password, and email
                - quit: Exit the application
                - help: Lists all commands
                """.stripIndent();
        serverFacade.request("help");

        Assertions.assertEquals(exptected, outputStreamCaptor.toString().strip());
    }



}
