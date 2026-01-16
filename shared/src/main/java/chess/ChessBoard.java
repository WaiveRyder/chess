package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] boardArray;
    private ChessPiece.PieceType[] backRow;

    public ChessBoard() {
        boardArray = new ChessPiece[9][9];
        initBackRow();
    }

    private void initBackRow(){
        backRow = new ChessPiece.PieceType[9];
        backRow[1] = ChessPiece.PieceType.ROOK;
        backRow[2] = ChessPiece.PieceType.KNIGHT;
        backRow[3] = ChessPiece.PieceType.BISHOP;
        backRow[4] = ChessPiece.PieceType.QUEEN;
        backRow[5] = ChessPiece.PieceType.KING;
        backRow[6] = ChessPiece.PieceType.BISHOP;
        backRow[7] = ChessPiece.PieceType.KNIGHT;
        backRow[8] = ChessPiece.PieceType.ROOK;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        boardArray[position.getRow()][position.getColumn()] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return boardArray[position.getRow()][position.getColumn()];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        resetBackRow(ChessGame.TeamColor.WHITE);
        resetBackRow(ChessGame.TeamColor.BLACK);
        resetPawns();
        resetMiddle();
    }

    /**
     * Resets the back row of a color for a new game
     *
     * @param color The color of which team gets reset
     */
    private void resetBackRow(ChessGame.TeamColor color){
        int row = 1;
        if(color == ChessGame.TeamColor.BLACK){
            row = 8;
        }
        for(int i = 1; i < 9; i++){
            this.addPiece(new ChessPosition(row, i), new ChessPiece(color, backRow[i]));
        }
    }

    /**
     * Resets both colors pawns
     */
    private void resetPawns(){
        ChessGame.TeamColor color = ChessGame.TeamColor.WHITE;
        for (int i = 2; i < 8; i++){
            for (int j = 1; j < 9; j++){
                this.addPiece(new ChessPosition(i, j), new ChessPiece(color, ChessPiece.PieceType.PAWN));
                if (i == 2 && j == 8) { i = 6; color = ChessGame.TeamColor.BLACK; }
            }
        }
    }

    /**
     * Resets the middle spaces on the board to empty/null
     */
    private void resetMiddle(){
        for(int i = 3; i < 7; i++){
            for(int j = 1; j < 9; j++){
                boardArray[i][j] = null;
            }
        }
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();

        for(int i = 8; i > 0; i--){
            for(int j = 1; j < 9; j++){
                ChessPiece piece = boardArray[i][j];
                ChessGame.TeamColor color;
                ChessPiece.PieceType pieceType;
                if(piece != null){
                    color = piece.getTeamColor();
                    pieceType = piece.getPieceType();
                } else {
                    color = null;
                    pieceType = null;
                }


                String nextPiece = switch (pieceType) {
                    case KING -> "K";
                    case QUEEN -> "Q";
                    case BISHOP -> "B";
                    case KNIGHT -> "N";
                    case ROOK -> "R";
                    case PAWN -> "P";
                    case null -> " ";
                };
                if(!nextPiece.equals(" ") && color == ChessGame.TeamColor.BLACK){
                    nextPiece = nextPiece.toLowerCase();
                }

                builder.append("|").append(nextPiece);
                if(j == 8){
                    builder.append("|");
                    if(i != 1){
                        builder.append("\n");
                    }
                }
            }
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(boardArray, that.boardArray);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(boardArray);
    }
}
