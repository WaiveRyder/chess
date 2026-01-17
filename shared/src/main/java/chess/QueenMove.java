package chess;

import java.util.ArrayList;
import java.util.Collection;

public class QueenMove {
    private ChessBoard board;
    private ChessPosition myPosition;
    private ChessGame.TeamColor color;
    private ArrayList<ChessMove> queenMoves;

    public QueenMove(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color) {
        this.board = board;
        this.myPosition = myPosition;
        this.color = color;
    }

    public Collection<ChessMove> getQueenMoves(){
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        int[] gridRow = {0, 1, 1, 1, 0, -1, -1, -1};
        int[] gridCol = {-1, -1, 0, 1, 1, 1, 0, -1};

        queenMoves = new ArrayList<ChessMove>();

        int newRow;
        int newCol;
        ChessPosition newPosition;
        for(int i = 0; i < 8; i++){

            newRow = myRow+gridRow[i];
            newCol = myCol+gridCol[i];
            while((newRow < 9 && newRow > 0) && (newCol < 9 && newCol > 0)){

                newPosition = new ChessPosition(newRow, newCol);
                if(board.getPiece(newPosition) == null){
                    queenMoves.add(new ChessMove(myPosition, newPosition, null));
                } else if(board.getPiece(newPosition).getTeamColor() != color){
                    queenMoves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                } else {
                    break;
                }

                newRow += gridRow[i];
                newCol += gridCol[i];
            }
        }
        return queenMoves;
    }
}
