package passoff.chess;

import chess.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PersonalTests {
    @Test
    @DisplayName("Test toString Empty ChessBoard")
    public void toStringEmptyChessBoard() {
        ChessBoard board = new ChessBoard();
        String actualBoard = board.toString();
        String expectedBoard = """
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |""";
        Assertions.assertEquals(expectedBoard, actualBoard);
    }

    @Test
    @DisplayName("Test toString Reset ChessBoard")
    public void toStringResetChessBoard() {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        String actualBoard = board.toString();
        String expectedBoard = """
                |r|n|b|q|k|b|n|r|
                |p|p|p|p|p|p|p|p|
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                |P|P|P|P|P|P|P|P|
                |R|N|B|Q|K|B|N|R|""";
        Assertions.assertEquals(expectedBoard, actualBoard);
    }

    @Test
    @DisplayName("PawnMove One Space")
    public void pawnMoveOneSpace(){
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        ChessPosition position = new ChessPosition(2, 2);
        Collection<ChessMove> actualMoves = board.getPiece(position).pieceMoves(board, position);

        Collection<ChessMove> expectedMoves = new ArrayList<ChessMove>();
        expectedMoves.add(new ChessMove(new ChessPosition(2, 2), new ChessPosition(3, 2), null));
        expectedMoves.add(new ChessMove(new ChessPosition(2, 2), new ChessPosition(4, 2), null));

        Assertions.assertEquals(expectedMoves, actualMoves);
    }

    @Test
    @DisplayName("KingMove Possible")
    public void kingMovePossible(){
        ChessBoard board = new ChessBoard();

        ChessPosition position1 = new ChessPosition(1, 1);
        board.addPiece(position1, new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        board.addPiece(new ChessPosition(2, 1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));

        Collection<ChessMove> actualMoves1 = board.getPiece(position1).pieceMoves(board, position1);
        Collection<ChessMove> expectedMoves1 = new ArrayList<ChessMove>();
        expectedMoves1.add(new ChessMove(position1, new ChessPosition(1, 2), null));
        expectedMoves1.add(new ChessMove(position1, new ChessPosition(2, 1), null));
        expectedMoves1.add(new ChessMove(position1, new ChessPosition(2, 2), null));

        Assertions.assertEquals(expectedMoves1, actualMoves1);


        board.resetBoard();

        ChessPosition position2 = new ChessPosition(1, 5);
        Collection<ChessMove> actualMoves2 = board.getPiece(position2).pieceMoves(board, position2);

        Collection<ChessMove> expectedMoves2 = new ArrayList<ChessMove>();

        Assertions.assertEquals(expectedMoves2, actualMoves2);
    }
}
