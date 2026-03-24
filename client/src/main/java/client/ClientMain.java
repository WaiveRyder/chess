package client;

import chess.*;

import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        System.out.println("Welcome to Chess! Type 'help' for a list of commands.");

        Scanner scanner = new Scanner(System.in);

        State state = State.PRE_LOGIN;
        ServerFacade serverFacade = new ServerFacade(8080);


        while (true) {
            String input = scanner.nextLine();

            if (input.equals("draw white")) {
                ChessBoard board = new ChessBoard();
                board.resetBoard();
                ClientDraw.drawBoard(board, ChessGame.TeamColor.WHITE);
                continue;
            } else if (input.equals("draw black")) {
                ChessBoard board = new ChessBoard();
                board.resetBoard();
                ClientDraw.drawBoard(board, ChessGame.TeamColor.BLACK);
                System.out.println(board);
                continue;
            }
            serverFacade.request(input.split(" "));

            if (input.equalsIgnoreCase("quit") && serverFacade.state == State.PRE_LOGIN) {
                break;
            }
        }

        scanner.close();
    }
}
