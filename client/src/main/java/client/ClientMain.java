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
        ClientWS ws = new ClientWS(8080);


        while (true) {
            String input = scanner.nextLine();

            //serverFacade.request(input.split(" "));
            ws.sendMessage(input);

            if (input.equalsIgnoreCase("quit") && serverFacade.state == State.PRE_LOGIN) {
                break;
            }
        }

        scanner.close();
    }
}
