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
        int[] gridCol = {-1, 0, 1};

        ChessPiece.PieceType[] promotions = {
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT
        };

        pawnMoves = new ArrayList<ChessMove>();

        if(color == ChessGame.TeamColor.BLACK){
            lastRow = 1;
            firstRow = 7;
            direction = -1;
        }

        ChessPosition newPosition;

        for(int col : gridCol){
            newPosition = new ChessPosition(myRow+direction, myCol+col);
            //If enemy on either side, add option to take
            int newCol = myCol + col;
            if(col != 0 && newCol < 9 && newCol > 0 && board.getPiece(newPosition) != null && board.getPiece(newPosition).getTeamColor() != color){
                if(myRow+direction == lastRow){
                    for(ChessPiece.PieceType type : promotions){
                        pawnMoves.add(new ChessMove(myPosition, newPosition, type));
                    }
                } else {
                    pawnMoves.add(new ChessMove(myPosition, newPosition, null));
                }
                //Else if no one in front add option to move
            } else if(col == 0 && myCol+col < 9 && myCol+col > 0 && board.getPiece(newPosition) == null){
                if(myRow+direction == lastRow){
                    for(ChessPiece.PieceType type : promotions){
                        pawnMoves.add(new ChessMove(myPosition, newPosition, type));
                    }
                } else {
                    pawnMoves.add(new ChessMove(myPosition, newPosition, null));
                }

                //If the pawn has never moved, add the second square in front
                newPosition = new ChessPosition(myRow+2*direction, myCol);
                if(myPosition.getRow() == firstRow && board.getPiece(newPosition) == null){
                    pawnMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
        return pawnMoves;
    }
}
