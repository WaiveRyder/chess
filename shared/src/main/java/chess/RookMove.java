package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class RookMove {
    private ChessBoard board;
    private ChessPosition myPosition;
    private ChessGame.TeamColor color;
    private ArrayList<ChessMove> rookMoves;

    public RookMove(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color) {
        this.board = board;
        this.myPosition = myPosition;
        this.color = color;
    }

    public Collection<ChessMove> getRookMoves(){
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        int[] gridRow = {0, 1, 0, -1};
        int[] gridCol = {-1, 0, 1, 0};

        rookMoves = new ArrayList<ChessMove>();

        int newRow;
        int newCol;
        ChessPosition newPosition;
        for(int i = 0; i < 4; i++){

            newRow = myRow+gridRow[i];
            newCol = myCol+gridCol[i];
            while((newRow < 9 && newRow > 0) && (newCol < 9 && newCol > 0)){

                newPosition = new ChessPosition(newRow, newCol);
                if(board.getPiece(newPosition) == null){
                    rookMoves.add(new ChessMove(myPosition, newPosition, null));
                } else if(board.getPiece(newPosition).getTeamColor() != color){
                    rookMoves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                } else {
                    break;
                }

                newRow += gridRow[i];
                newCol += gridCol[i];
            }
        }
        return rookMoves;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RookMove rookMove = (RookMove) o;
        return Objects.equals(board, rookMove.board) && Objects.equals(myPosition, rookMove.myPosition) && color == rookMove.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, myPosition, color);
    }
}
