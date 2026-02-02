package chess;

import java.util.Collection;
import java.util.Vector;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    ChessBoard board;
    TeamColor teamTurn;

    Vector<ChessPiece.PieceType> QRList;
    Vector<ChessPiece.PieceType> QBPList;
    Vector<ChessPiece.PieceType> NList;

    int[] gridRowQR;
    int[] gridColQR;
    int[] gridRowQBP;
    int[] gridColQBP;
    int[] gridRowN;
    int[] gridColN;

    public ChessGame() {
        board = new ChessBoard();
        setBoard(board);
        teamTurn = TeamColor.WHITE;
        initCoords();
        initLists();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if(piece != null) {
            return piece.pieceMoves(board, startPosition);
        } else {
            return null;
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece piece = board.getPiece(startPosition);

        if(!validMoves(startPosition).contains(move)) {
            String builder = "Warning invalid move on " + piece +
                    " at " + startPosition +
                    " to " + endPosition;
            throw new InvalidMoveException(builder);
        } else {
            if(move.promotionPiece != null){
                piece = new ChessPiece(piece.getTeamColor(), move.promotionPiece);
            }
            board.addPiece(endPosition, piece);
            board.addPiece(startPosition, null);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = findKingPos(teamColor);

        if(kingPos == null) {return false;}

        return kingVectors(kingPos, teamColor, QRList, gridRowQR, gridColQR)
                || kingVectors(kingPos, teamColor, NList, gridRowN, gridColN)
                || kingVectors(kingPos, teamColor, QBPList, gridRowQBP, gridColQBP);
    }

    private boolean kingVectors(ChessPosition pos, TeamColor color, Vector<ChessPiece.PieceType> pieceList, int[] row, int[] col) {
        int newRow;
        int newCol;
        int myRow = pos.getRow();
        int myCol = pos.getColumn();
        for(int i = 0; i < row.length; i++) {
            newRow = myRow + row[i];
            newCol = myCol + col[i];
            while(newRow < 9 && newRow > 0 && newCol < 9 && newCol > 0){
                ChessPosition newPos = new ChessPosition(newRow, newCol);
                ChessPiece newPiece = board.getPiece(newPos);

                if(newPiece != null
                        && newPiece.getTeamColor() != color
                        && pieceList.contains(newPiece.getPieceType())) {
                    if (newPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
                        if (color == TeamColor.WHITE && newRow - myRow == 1 && col[i] != 0) {
                            return true;
                        } else if(color == TeamColor.BLACK && myRow-newRow == 1 && col[i] != 0) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                } else if(newPiece != null) {
                    break;
                }

                newRow += row[i];
                newCol += col[i];
            }
        }

        return false;
    }

    private ChessPosition findKingPos(TeamColor color) {
        for(int row = 1; row < 9; row++) {
            for(int col = 1; col < 9; col++) {
                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(newPos);
                if(piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == color) {
                    return newPos;
                }
            }
        }

        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private void initCoords() {
        gridRowQR = new int[]{0, 1, 0, -1};
        gridColQR = new int[]{-1, 0, 1, 0};

        gridRowQBP = new int[]{1, 1, -1, -1};
        gridColQBP = new int[]{-1, 1, 1, -1};

        gridRowN = new int[]{1, 2, 2, 1, -1, -2, -2, -1};
        gridColN = new int[]{-2, -1, 1, 2, 2, 1, -1, -2};
    }

    private void initLists() {
        QRList = new Vector<>();
        QRList.add(ChessPiece.PieceType.QUEEN);
        QRList.add(ChessPiece.PieceType.ROOK);

        QBPList = new Vector<>();
        QBPList.add(ChessPiece.PieceType.QUEEN);
        QBPList.add(ChessPiece.PieceType.BISHOP);
        QBPList.add(ChessPiece.PieceType.PAWN);

        NList = new Vector<>();
        NList.add(ChessPiece.PieceType.KNIGHT);
    }
}
