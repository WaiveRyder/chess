package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMove {
    private ChessBoard board;
    private ChessPosition myPosition;
    private ChessGame.TeamColor color;
    private ArrayList<ChessMove> knightMoves;

    public KnightMove(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color) {
        this.board = board;
        this.myPosition = myPosition;
        this.color = color;
    }

    public Collection<ChessMove> getKnightMoves() {
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        int[] gridRow = {-1, 1, 2, 2, 1, -1, -2, -2};
        int[] gridCol = {-2, -2, -1, 1, 2, 2, 1, -1};

        knightMoves = new ArrayList<ChessMove>();

        int newRow;
        int newCol;
        ChessPosition newPosition;

        for (int i = 0; i < 8; i++) {
            newRow = myRow + gridRow[i];
            newCol = myCol + gridCol[i];

            if (newRow < 9 && newRow > 0 && newCol < 9 && newCol > 0) {
                newPosition = new ChessPosition(newRow, newCol);
                if (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != color) {
                    knightMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
        return knightMoves;
    }
}
