package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] boardArray;

    public ChessBoard() {
        boardArray = new ChessPiece[8][8];
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
    }

    /**
     * Resets the back row of a color for a new game
     *
     * @param color The color of which team gets reset
     */
    private void resetBackRow(ChessGame.TeamColor color){
        int row = 0;
        if(color == ChessGame.TeamColor.BLACK){
            row = 7;
        }
        boardArray[row][0] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
        boardArray[row][1] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
        boardArray[row][2] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
        boardArray[row][3] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
        boardArray[row][4] = new ChessPiece(color, ChessPiece.PieceType.QUEEN);
        boardArray[row][5] = new ChessPiece(color, ChessPiece.PieceType.KING);
        boardArray[row][6] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
        boardArray[row][7] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
        boardArray[row][8] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
    }

    /**
     * Resets both colors pawns
     */
    private void resetPawns(){
        ChessGame.TeamColor color = ChessGame.TeamColor.WHITE;
        for (int i = 1; i < 7; i++){
            for (int j = 0; j < 8; j++){
                boardArray[i][j] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
                if (i == 1) { i = 6; color = ChessGame.TeamColor.BLACK; }
            }
        }
    }
}
