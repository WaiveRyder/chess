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
        boardArray = new ChessPiece[9][9];
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
        int row = 0;
        if(color == ChessGame.TeamColor.BLACK){
            row = 7;
        }
        boardArray[row][1] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
        boardArray[row][2] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
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
        for (int i = 2; i < 8; i++){
            for (int j = 1; j < 9; j++){
                boardArray[i][j] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
                if (i == 2) { i = 7; color = ChessGame.TeamColor.BLACK; }
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
}
