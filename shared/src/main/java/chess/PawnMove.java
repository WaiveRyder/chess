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

        if(color == ChessGame.TeamColor.BLACK){
            lastRow = 1;
            firstRow = 7;
            direction = -1;
        }

        pawnMoves = new ArrayList<ChessMove>();
        if(myPosition.getRow() <= lastRow-direction){
            pawnMoves.add(new ChessMove(myPosition, new ChessPosition(myRow+direction, myCol), null));
        }
        if(myPosition.getRow() == firstRow && board.getPiece(new ChessPosition(myRow+direction, myCol)) == null){
            pawnMoves.add(new ChessMove(myPosition, new ChessPosition(myRow+2*direction, myCol), null));
        }

        return pawnMoves;
    }
}
