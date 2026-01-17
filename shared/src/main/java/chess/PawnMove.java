package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class PawnMove {

    private ChessBoard board;
    private ChessPosition myPosition;
    private ChessGame.TeamColor color;
    private ArrayList<ChessMove> pawnMoves;

    public PawnMove(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color) {
        this.board = board;
        this.myPosition = myPosition;
        this.color = color;
    }

    public Collection<ChessMove> getPawnMoves(){
        int lastRow = 8;
        int firstRow = 2;
        int direction = 1;

        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();

        pawnMoves = new ArrayList<ChessMove>();

        if(color == ChessGame.TeamColor.BLACK){
            lastRow = 1;
            firstRow = 7;
            direction = -1;
        }

        if(myPosition.getRow() <= lastRow-direction){
            ChessPosition newPosition = new ChessPosition(myRow+direction, myCol);

            if(board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != color){
                pawnMoves.add(new ChessMove(myPosition, new ChessPosition(myRow+direction, myCol), null));
            }

            newPosition = new ChessPosition(myRow+2*direction, myCol);
            if(myPosition.getRow() == firstRow && (board.getPiece(newPosition) == null || board.getPiece(newPosition).getTeamColor() != color)){
                pawnMoves.add(new ChessMove(myPosition, newPosition, null));
            }
        }

        return pawnMoves;
    }
}
