package client;
import jakarta.websocket.*;

import java.net.URI;

@ClientEndpoint
public class ClientWS {
    private Session session;

    public ClientWS(int port) {
        try {
            URI uri = new URI("ws://localhost:" + port + "/ws");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, uri);
        } catch (Exception e) {
            System.out.println("Error creating URI: " + e.getMessage());
        }

    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to websocket");
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
    }

    public void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        } else {
            System.out.println("Error: Websocket is not open. Cannot send message.");
        }
    }
}
