package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMove {
    private ChessBoard board;
    private ChessPosition myPosition;
    private ChessGame.TeamColor color;
    private ArrayList<ChessMove> bishopMoves;

    public BishopMove(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color) {
        this.board = board;
        this.myPosition = myPosition;
        this.color = color;
    }

    public Collection<ChessMove> getBishopMoves(){
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        int[] gridRow = {-1, 1, 1, -1};
        int[] gridCol = {-1, -1, 1, 1};

        bishopMoves = new ArrayList<ChessMove>();

        int newRow;
        int newCol;
        ChessPosition newPosition;

        for(int i = 0; i < 4; i++){
            newRow = myRow + gridRow[i];
            newCol = myCol + gridCol[i];
            while(newRow > 0 && newRow < 9 && newCol > 0 && newCol < 9){
                newPosition = new ChessPosition(newRow, newCol);
                if(board.getPiece(newPosition) == null){
                    bishopMoves.add(new ChessMove(myPosition, newPosition, null));
                } else if(board.getPiece(newPosition).getTeamColor() != color){
                    bishopMoves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                } else {
                    break;
                }

                newRow += gridRow[i];
                newCol += gridCol[i];
            }
        }
        return bishopMoves;
    }
}
