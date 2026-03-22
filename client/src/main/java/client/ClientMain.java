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
        ClientDraw clientDraw = new ClientDraw();
        State state = State.PRE_LOGIN;

        while (true) {
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("help")) {
                clientDraw.draw(input, state);
            } else if (input.equalsIgnoreCase("quit")) {
                System.out.println("Thanks for playing! Catch you next time.");
                break;
            } else {
                System.out.println("I didn't quite catch that. Type 'help' for a list of commands.");
            }
        }

        scanner.close();
    }
}
