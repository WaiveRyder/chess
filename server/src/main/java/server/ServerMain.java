package server;

import chess.*;

// Windows -> services
// MySQL84, start the service
// Windows -> MySQL
// \connect root@localhost:3306

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server();
        server.run(8080);

        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
    }
}
