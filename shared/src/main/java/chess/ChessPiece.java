package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        ChessPiece test = board.getPiece(myPosition);
        List<ChessMove> possibleMoves = new ArrayList<>();

        if(test.getPieceType() == PieceType.ROOK){
            possibleMoves = rookMoves(board, myPosition);
        }if(test.getPieceType() == PieceType.KNIGHT){
            possibleMoves = knightMoves(board, myPosition);
        }

        if(test.getPieceType() == PieceType.BISHOP){
            for(int i = 1; i < 8; i++){
                ChessPosition newPosition = new ChessPosition(myPosition.getRow() + i, myPosition.getColumn() + i);
                ChessMove newOne = new ChessMove(myPosition, newPosition, null);
                possibleMoves.add(newOne);
            }

            for(int i = 1; i < 8; i++){
                ChessPosition newPosition = new ChessPosition(myPosition.getRow() - i, myPosition.getColumn() + i);
                ChessMove newOne = new ChessMove(myPosition, newPosition, null);
                possibleMoves.add(newOne);
            }

            for(int i = 1; i < 8; i++){
                ChessPosition newPosition = new ChessPosition(myPosition.getRow() + i, myPosition.getColumn() - i);
                ChessMove newOne = new ChessMove(myPosition, newPosition, null);
                possibleMoves.add(newOne);
            }

            for(int i = 1; i < 8; i++){
                ChessPosition newPosition = new ChessPosition(myPosition.getRow() - i, myPosition.getColumn() - i);
                ChessMove newOne = new ChessMove(myPosition, newPosition, null);
                possibleMoves.add(newOne);
            }

        }

        return possibleMoves;
    }

    private List<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();

        ChessMove newOne = knightJumps(myPosition, 1, 2);
        ChessMove newOne1 = knightJumps(myPosition, 1, -2);
        ChessMove newOne2 = knightJumps(myPosition, 2, 1);
        ChessMove newOne3 = knightJumps(myPosition, -2, 1);
        ChessMove newOne4 = knightJumps(myPosition, -1, -2);
        ChessMove newOne5 = knightJumps(myPosition, -1, 2);
        ChessMove newOne6 = knightJumps(myPosition, -2, -1);
        ChessMove newOne7 = knightJumps(myPosition, 2, -1);
        possibleMoves.add(newOne);
        possibleMoves.add(newOne1);
        possibleMoves.add(newOne2);
        possibleMoves.add(newOne3);
        possibleMoves.add(newOne4);
        possibleMoves.add(newOne5);
        possibleMoves.add(newOne6);
        possibleMoves.add(newOne7);

        for(int i = 0; i < possibleMoves.size();){
            if(possibleMoves.get(i).getEndPosition().getRow() > 8 || possibleMoves.get(i).getEndPosition().getRow() <= 0){
                possibleMoves.remove(i);
            }else if (possibleMoves.get(i).getEndPosition().getColumn() > 8 || possibleMoves.get(i).getEndPosition().getColumn() <= 0) {
                possibleMoves.remove(i);
            }else{
                i++;
            }
        }

        for(int i = 0; i < possibleMoves.size();){
            if(board.getPiece(possibleMoves.get(i).getEndPosition()) != null ) {
                if(board.getPiece(possibleMoves.get(i).getEndPosition()).getTeamColor() != board.getPiece(possibleMoves.get(i).getStartPosition()).getTeamColor()) {
                    i++;
                }else{
                    possibleMoves.remove(i);
                }

            }else{
                i++;
            }
        }

        return possibleMoves;
    }

    public ChessMove knightJumps(ChessPosition myPosition, int jumpRow, int jumpCol){
        ChessPosition newPosition = new ChessPosition(myPosition.getRow() + jumpRow, myPosition.getColumn() + jumpCol);
        return new ChessMove(myPosition, newPosition, null);
    }

    public List<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition){
        List<ChessMove> possibleMoves = new ArrayList<>();

        int position = 1;
        int backPosition = 1;
        int rightPosition = 1;
        int leftPosition = 1;


        while(position < 7){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + position, myPosition.getColumn());
            ChessMove newOne = new ChessMove(myPosition, newPosition, null);
            if(newOne.getEndPosition().getRow() > 8){
                break;
            }

            possibleMoves.add(newOne);
            position ++;
        }

        while(backPosition < 7){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() - backPosition, myPosition.getColumn());
            ChessMove newOne = new ChessMove(myPosition, newPosition, null);
            if(newOne.getEndPosition().getRow() <= 0){
                break;
            }
            possibleMoves.add(newOne);
            backPosition ++;
        }

        while(rightPosition < 7){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn() + rightPosition);
            ChessMove newOne = new ChessMove(myPosition, newPosition, null);
            if(newOne.getEndPosition().getColumn() > 8){
                break;
            }
            possibleMoves.add(newOne);
            rightPosition ++;
        }

        while(leftPosition < 7){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn() - leftPosition);
            ChessMove newOne = new ChessMove(myPosition, newPosition, null);
            if(newOne.getEndPosition().getColumn() <= 0){
                break;
            }
            possibleMoves.add(newOne);
            leftPosition ++;
        }

        for(int i = 0; i < possibleMoves.size();){
            if(board.getPiece(possibleMoves.get(i).getEndPosition()) != null ) {
                if(board.getPiece(possibleMoves.get(i).getEndPosition()).getTeamColor() != board.getPiece(possibleMoves.get(i).getStartPosition()).getTeamColor()) {

                    for(int k = i + 1; k < possibleMoves.size();){
                        if(possibleMoves.get(i).getEndPosition().getRow() == possibleMoves.get(k).getEndPosition().getRow()){
                            possibleMoves.remove(k);
                        }else{
                            k++;
                        }
                    }

                    for(int j = i + 1; j < possibleMoves.size();){
                        if(possibleMoves.get(i).getEndPosition().getColumn() == possibleMoves.get(j).getEndPosition().getColumn()){
                            possibleMoves.remove(j);
                        }else{
                            j++;
                        }
                    }
                    i++;
                }else{
                    for(int k = i + 1; k < possibleMoves.size();){
                        if(possibleMoves.get(i).getEndPosition().getRow() == possibleMoves.get(k).getEndPosition().getRow()){
                            possibleMoves.remove(k);
                        }else{
                            k++;
                        }
                    }
                    for(int j = i + 1; j < possibleMoves.size();){
                        if(possibleMoves.get(i).getEndPosition().getColumn() == possibleMoves.get(j).getEndPosition().getColumn()){
                            if(possibleMoves.get(i).getEndPosition().getRow() > possibleMoves.get(j).getEndPosition().getRow()){
                                possibleMoves.remove(j);
                            }else{
                                j++;
                            }
                        }else{
                            j++;
                        }
                    }
                    possibleMoves.remove(i);
                }
            }else{
                i++;
            }
        }

        return possibleMoves;
    }





    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
