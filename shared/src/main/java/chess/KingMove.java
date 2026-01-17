package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class KingMove {

    private ChessBoard board;
    private ChessPosition myPosition;
    private ChessGame.TeamColor color;
    private ArrayList<ChessMove> kingMoves;

    public KingMove(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color) {
        this.board = board;
        this.myPosition = myPosition;
        this.color = color;
    }

    public Collection<ChessMove> getKingMoves(){
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        int[] grid = {-1, 0, 1};

        kingMoves = new ArrayList<ChessMove>();

        for(int row : grid){
            for(int col : grid){
                if(row == 0 && col == 0){
                    continue;
                }
                if((myRow+row > 0 && myRow+row < 9) && (myCol+col > 0 && myCol+col < 9)){
                    ChessPosition newPosition = new ChessPosition(myRow+row, myCol+col);
                    if(board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != color){
                        kingMoves.add(new ChessMove(myPosition, newPosition, null));
                    }

                }
            }
        }
        return kingMoves;
    }
}
