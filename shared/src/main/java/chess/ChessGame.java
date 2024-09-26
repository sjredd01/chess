package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamColor;

    public ChessGame() {

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamColor = team;
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
        ChessPiece myPiece = getBoard().getPiece(startPosition);
        if(myPiece == null){
            return null;
        }

        Collection<ChessMove> possibleMoves;
        possibleMoves = myPiece.pieceMoves(getBoard(), startPosition);
        TeamColor color = myPiece.getTeamColor();

        possibleMoves.removeIf(move -> willBeInCheck(color, move, myPiece));

        if(myPiece.getPieceType() == ChessPiece.PieceType.KING){

            Collection<ChessMove> enemyMoves = possibleEnemyMoves(board, color);
            for(ChessMove enemy : enemyMoves){
                possibleMoves.removeIf(move -> move.getEndPosition().equals(enemy.getEndPosition()));
            }
        }

        return possibleMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        try {
            Collection<ChessMove> valid = validMoves(move.getStartPosition());
        } catch (NullPointerException e) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> valid = validMoves(move.getStartPosition());

        boolean isValid = false;

        for(ChessMove validMove : valid){
            if(validMove.equals(move)){
                isValid = true;
                break;
            }
        }

        if(!isValid){
            throw new InvalidMoveException();
        }

        if((move.getEndPosition().getRow() == 8 || move.getEndPosition().getRow() == 1) && board.getPiece(move.getStartPosition()).getPieceType() == ChessPiece.PieceType.PAWN){
            TeamColor pieceColor = board.getPiece(move.getStartPosition()).getTeamColor();
            ChessPiece promotion = new ChessPiece(pieceColor, move.getPromotionPiece());
            board.addPiece(new ChessPosition(move.getEndPosition().getRow(), move.getEndPosition().getColumn()), promotion);;
            board.removePiece(move.getStartPosition());
        }else{
            board.addPiece(new ChessPosition(move.getEndPosition().getRow(), move.getEndPosition().getColumn()), board.getPiece(move.getStartPosition()));;
            board.removePiece(move.getStartPosition());
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {

        ChessPosition kingPosition = kingPosition(teamColor);
        Collection<ChessMove> enemyMoves = possibleEnemyMoves(board, teamColor);

        for(ChessMove possibleMove : enemyMoves){
            if(possibleMove.getEndPosition().equals(kingPosition)){
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {

        ChessPosition kingPosition = kingPosition(teamColor);

        return isInCheck(teamColor) && validMoves(kingPosition).isEmpty();

    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
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

    public ChessPosition kingPosition(TeamColor teamColor){
        for(int i = 1; i <= 8; i++){
            for(int k = 1; k <= 8; k ++){
                if(board.getPiece(new ChessPosition(i,k)) == null){
                    continue;
                }
                if((board.getPiece(new ChessPosition(i,k)).getPieceType() == ChessPiece.PieceType.KING) && (board.getPiece(new ChessPosition(i,k)).getTeamColor() == teamColor)){
                    return new ChessPosition(i,k);
                }
            }
        }
        return null;
    }

    public Collection<ChessMove> possibleEnemyMoves(ChessBoard board, TeamColor teamColor){
        Collection<ChessMove> possibleMove = new ArrayList<>(List.of());
        Collection<ChessMove> possibleMove1;

        for(int i = 1; i <= 8; i++){
            for(int k = 1; k <= 8; k ++){
                if(board.getPiece(new ChessPosition(i,k)) == null){
                    continue;
                }
                if((board.getPiece(new ChessPosition(i,k)).getTeamColor() != teamColor)){
                    possibleMove1 = board.getPiece(new ChessPosition(i,k)).pieceMoves(board, new ChessPosition(i,k));

                    possibleMove.addAll(possibleMove1);
                }
            }
        }

        return possibleMove;

    }

    public boolean willBeInCheck(TeamColor teamColor, ChessMove move, ChessPiece myPiece) {
        this.teamColor = teamColor;

        ChessPiece toSave = null;

        boolean pieceToSave = false;
        if(board.getPiece(move.getEndPosition()) != null){
            toSave = board.getPiece(move.getEndPosition());
            pieceToSave = true;
        }

        board.removePiece(move.getStartPosition());
        board.addPiece(move.getEndPosition(), myPiece);

        ChessPosition kingPosition = kingPosition(teamColor);
        Collection<ChessMove> enemyMoves = possibleEnemyMoves(board, teamColor);

        for(ChessMove possibleMove : enemyMoves){
            if(possibleMove.getEndPosition().equals(kingPosition)){
                board.removePiece(move.getEndPosition());
                board.addPiece(move.getStartPosition(), myPiece);
                if(pieceToSave){
                    board.addPiece(move.getEndPosition(), toSave);
                }
                return true;
            }
        }
        board.removePiece(move.getEndPosition());
        board.addPiece(move.getStartPosition(), myPiece);
        if(pieceToSave){
            board.addPiece(move.getEndPosition(), toSave);
        }
        return false;
    }

}
