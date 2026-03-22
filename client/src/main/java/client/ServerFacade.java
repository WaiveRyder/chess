package client;

import java.net.http.HttpClient;

public class ServerFacade {
    int port;
    HttpClient client;

    public ServerFacade(int port) {
        this.port = port;
        client = HttpClient.newHttpClient();
    }

    public void request(String... args) {
        if (args.length == 0) {
            ClientDraw.printError("No command provided");
        }
    }

}
