package passoff.chess;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
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
}
