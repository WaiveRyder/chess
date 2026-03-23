package client;

import chess.*;

import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        System.out.println("Welcome to Chess! Type 'help' for a list of commands.");

        Scanner scanner = new Scanner(System.in);

        ServerFacade serverFacade = new ServerFacade(8080);
        State state = State.PRE_LOGIN;

        while (true) {
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("help")) {
                ClientDraw.draw(input, state);
            } else if (input.equalsIgnoreCase("quit")) {
                ClientDraw.draw(input, state);
                break;
            } else {
                serverFacade.request(state, input.split(" "));
            }
        }

        scanner.close();
    }
}
