package client;
import com.google.gson.Gson;
import jakarta.websocket.*;
import ui.ClientDraw;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.ConnectException;
import java.net.URI;

@ClientEndpoint
public class ClientWS {
    private Session session;
    Gson gson;

    public ClientWS(int port) {
        try {
            URI uri = new URI("ws://localhost:" + port + "/ws");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, uri);
            gson = new Gson();
        } catch (Exception e) {
            System.out.println("Error creating URI: " + e.getMessage());
        }

    }

    @OnOpen
    public void onOpen(Session session) {
        ClientDraw.draw("message", State.POST_LOGIN, "Connected to server");
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
        ClientDraw.draw("message", State.POST_LOGIN, serverMessage.getMessage());
    }

    private void sendMessage(
            UserGameCommand.CommandType type,
            String authToken,
            Integer gameID,
            String message
    ) throws ConnectException {
        if (session != null && session.isOpen()) {
            UserGameCommand command= new UserGameCommand(type, authToken, gameID);
            session.getAsyncRemote().sendText(gson.toJson(command));
        } else {
            throw new ConnectException("Error: Websocket session is not open.");
        }
    }

    public void connect(String authToken, Integer gameID, String message) throws ConnectException {
        sendMessage(UserGameCommand.CommandType.CONNECT, authToken, gameID, message);
    }

    public void makeMove(String authToken, Integer gameID, String message) throws ConnectException {
        sendMessage(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, message);
    }

    public void leave(String authToken, Integer gameID) throws ConnectException {
        sendMessage(UserGameCommand.CommandType.LEAVE, authToken, gameID, ".");
    }

    public void resign(String authToken, Integer gameID) throws ConnectException {
        sendMessage(UserGameCommand.CommandType.RESIGN, authToken, gameID, ".");
    }

    public void close() {
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                System.out.println("Error: Couldn't close websocket: " + e.getMessage());
            }
        }
    }
}
