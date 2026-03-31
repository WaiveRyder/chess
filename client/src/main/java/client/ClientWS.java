package client;
import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
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
    ChessGame.TeamColor playerColor;
    Gson gson;

    ChessGame game;

    public ClientWS(int port) {
        try {
            URI uri = new URI("ws://localhost:" + port + "/ws");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, uri);
            gson = new Gson();
            game = new ChessGame();
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

        if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            game = serverMessage.getGame();
            ClientDraw.drawBoard(game.getBoard(), playerColor);
        } else if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
            ClientDraw.printError(serverMessage.getErrorMessage());
        } else {
            ClientDraw.draw("message", State.POST_LOGIN, serverMessage.getMessage());
        }
    }

    public void setColor(ChessGame.TeamColor color) {
        playerColor = color;
    }

    private void sendMessage(
            UserGameCommand.CommandType type,
            String authToken,
            Integer gameID,
            String message,
            ChessMove move
    ) throws ConnectException {
        if (session != null && session.isOpen()) {
            UserGameCommand command = new UserGameCommand(type, authToken, gameID, message, move);
            session.getAsyncRemote().sendText(gson.toJson(command));
        } else {
            throw new ConnectException("Error: Websocket session is not open.");
        }
    }

    public void connect(String authToken, Integer gameID, String message) throws ConnectException {
        sendMessage(UserGameCommand.CommandType.CONNECT, authToken, gameID, message, null);
    }

    public void makeMove(String authToken, Integer gameID,String message,ChessMove move) throws ConnectException{
        sendMessage(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, message, move);
    }

    public void leave(String authToken, Integer gameID, String color) throws ConnectException {
        sendMessage(UserGameCommand.CommandType.LEAVE, authToken, gameID, color, null);
    }

    public void resign(String authToken, Integer gameID) throws ConnectException {
        sendMessage(UserGameCommand.CommandType.RESIGN, authToken, gameID, "", null);
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
