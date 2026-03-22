package client;

import java.net.http.HttpClient;

public class ServerFacade {
    int port;
    HttpClient client;

    public ServerFacade(int port) {
        this.port = port;
        client = HttpClient.newHttpClient();
    }
}
