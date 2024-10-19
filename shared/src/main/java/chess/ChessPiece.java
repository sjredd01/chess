package chess;

import java.util.ArrayList;
import java.util.Collection;
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

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
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
        ArrayList<ChessMove> possibleMove = new ArrayList<>();

        if(board.getPiece(myPosition).type == PieceType.KNIGHT){
            possibleMove = knightMove(board, myPosition, possibleMove);
        }
        if(board.getPiece(myPosition).type == PieceType.KING){
            possibleMove = kingMove(board, myPosition, possibleMove);
        }
        if(board.getPiece(myPosition).type == PieceType.PAWN){
            if(board.getPiece(myPosition).pieceColor == ChessGame.TeamColor.WHITE) {
                possibleMove = pawnMove(board, myPosition, possibleMove);
            }else{
                possibleMove = pawnMove2(board, myPosition, possibleMove);
            }
        }
        if(board.getPiece(myPosition).type == PieceType.ROOK){
            possibleMove = rookMove(board, myPosition, possibleMove);
        }
        if(board.getPiece(myPosition).type == PieceType.BISHOP){
            possibleMove = bishopMove(board, myPosition, possibleMove);
        }
        if(board.getPiece(myPosition).type == PieceType.QUEEN){
            possibleMove = queenMove(board, myPosition, possibleMove);
        }


        return possibleMove;
    }

    public ChessMove pieceMove(ChessPosition myPosition, int upDown, int leftRight){
        ChessPosition newPosition = new ChessPosition(myPosition.getRow() + upDown, myPosition.getColumn() + leftRight );
        return new ChessMove(myPosition,newPosition,null);
    }

    public ArrayList<ChessMove> removeOutside(ArrayList<ChessMove> possibleMove){
        for(int i = 0; i < possibleMove.size();){
            if(possibleMove.get(i).getEndPosition().getRow() < 1 || possibleMove.get(i).getEndPosition().getRow() > 8){
                possibleMove.remove(i);
            } else if (possibleMove.get(i).getEndPosition().getColumn() < 1 || possibleMove.get(i).getEndPosition().getColumn() > 8) {
                possibleMove.remove(i);
            } else{
                i++;
            }
        }

        return possibleMove;
    }

    public ArrayList<ChessMove> captureEnemy(ChessBoard board, ArrayList<ChessMove> possibleMove){

        for(int i = 0; i < possibleMove.size();){
            if(board.getPiece(possibleMove.get(i).getEndPosition()) != null){
                if(board.getPiece(possibleMove.get(i).getStartPosition()).getTeamColor() == board.getPiece(possibleMove.get(i).getEndPosition()).getTeamColor()){
                    possibleMove.remove(i);
                }else{
                    i++;
                }
            }else{
                i++;
            }
        }


        return possibleMove;
    }

    public ArrayList<ChessMove> knightMove(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> possibleMove){
        possibleMove.add(pieceMove(myPosition, 1,2));
        possibleMove.add(pieceMove(myPosition, 1,-2));
        possibleMove.add(pieceMove(myPosition, 2,1));
        possibleMove.add(pieceMove(myPosition, 2,-1));
        possibleMove.add(pieceMove(myPosition, -1,2));
        possibleMove.add(pieceMove(myPosition, -1,-2));
        possibleMove.add(pieceMove(myPosition, -2,1));
        possibleMove.add(pieceMove(myPosition, -2,-1));

        possibleMove = removeOutside(possibleMove);
        possibleMove = captureEnemy(board, possibleMove);

        return possibleMove;
    }

    public ArrayList<ChessMove> kingMove(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> possibleMove){
        possibleMove.add(pieceMove(myPosition, 1,0));
        possibleMove.add(pieceMove(myPosition, -1,0));
        possibleMove.add(pieceMove(myPosition, 0,1));
        possibleMove.add(pieceMove(myPosition, 0,-1));
        possibleMove.add(pieceMove(myPosition, 1,1));
        possibleMove.add(pieceMove(myPosition, 1,-1));
        possibleMove.add(pieceMove(myPosition, -1,1));
        possibleMove.add(pieceMove(myPosition, -1,-1));

        possibleMove = removeOutside(possibleMove);
        possibleMove = captureEnemy(board, possibleMove);

        return possibleMove;
    }

    public ArrayList<ChessMove> pawnMove(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> possibleMove){
        ChessPosition forward = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn()) ;
        ChessPosition forward2 = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn()) ;
        ChessPosition attack = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1) ;
        ChessPosition attack2 = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1) ;


        if(board.getPiece(myPosition).pieceColor == ChessGame.TeamColor.WHITE){
            if(board.getPiece(forward) == null){
                if(forward.getRow() == 8){
                    possibleMove.add(new ChessMove(myPosition, forward, PieceType.QUEEN));
                    possibleMove.add(new ChessMove(myPosition, forward, PieceType.BISHOP));
                    possibleMove.add(new ChessMove(myPosition, forward, PieceType.KNIGHT));
                    possibleMove.add(new ChessMove(myPosition, forward, PieceType.ROOK));
                }else{
                    possibleMove.add(new ChessMove(myPosition, forward, null));
                }

                if(myPosition.getRow() == 2 && board.getPiece(forward2) == null){
                    possibleMove.add(new ChessMove(myPosition, forward2, null));
                }

            }
            if(attack2.getColumn() <= 8 && board.getPiece(attack2) != null && board.getPiece(attack2).pieceColor == ChessGame.TeamColor.BLACK){
                if(attack2.getRow() == 8){
                    possibleMove.add(new ChessMove(myPosition, attack2, PieceType.QUEEN));
                    possibleMove.add(new ChessMove(myPosition, attack2, PieceType.BISHOP));
                    possibleMove.add(new ChessMove(myPosition, attack2, PieceType.KNIGHT));
                    possibleMove.add(new ChessMove(myPosition, attack2, PieceType.ROOK));
                }else{
                    possibleMove.add(new ChessMove(myPosition,attack2,null));
                }
            }

            if(attack.getColumn() >= 1 && board.getPiece(attack) != null && board.getPiece(attack).pieceColor == ChessGame.TeamColor.BLACK){
                if(attack.getRow() == 8){
                    possibleMove.add(new ChessMove(myPosition, attack, PieceType.QUEEN));
                    possibleMove.add(new ChessMove(myPosition, attack, PieceType.BISHOP));
                    possibleMove.add(new ChessMove(myPosition, attack, PieceType.KNIGHT));
                    possibleMove.add(new ChessMove(myPosition, attack, PieceType.ROOK));
                }else{
                    possibleMove.add(new ChessMove(myPosition,attack,null));
                }
            }
        }

        possibleMove = removeOutside(possibleMove);


        return possibleMove;

    }

    public ArrayList<ChessMove> pawnMove2(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> possibleMove){
        ChessPosition forward = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn()) ;
        ChessPosition forward2 = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn()) ;
        ChessPosition attack = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1) ;
        ChessPosition attack2 = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1) ;


        if(board.getPiece(myPosition).pieceColor == ChessGame.TeamColor.BLACK){
            if(board.getPiece(forward) == null){
                if(forward.getRow() == 1){
                    possibleMove.add(new ChessMove(myPosition, forward, PieceType.QUEEN));
                    possibleMove.add(new ChessMove(myPosition, forward, PieceType.BISHOP));
                    possibleMove.add(new ChessMove(myPosition, forward, PieceType.KNIGHT));
                    possibleMove.add(new ChessMove(myPosition, forward, PieceType.ROOK));
                }else{
                    possibleMove.add(new ChessMove(myPosition, forward, null));
                }

                if(myPosition.getRow() == 7 && board.getPiece(forward2) == null){
                    possibleMove.add(new ChessMove(myPosition, forward2, null));
                }



            }
            if(attack2.getColumn() <= 8 && board.getPiece(attack2) != null && board.getPiece(attack2).pieceColor == ChessGame.TeamColor.WHITE){
                if(attack2.getRow() == 1){
                    possibleMove.add(new ChessMove(myPosition, attack2, PieceType.QUEEN));
                    possibleMove.add(new ChessMove(myPosition, attack2, PieceType.BISHOP));
                    possibleMove.add(new ChessMove(myPosition, attack2, PieceType.KNIGHT));
                    possibleMove.add(new ChessMove(myPosition, attack2, PieceType.ROOK));
                }else{
                    possibleMove.add(new ChessMove(myPosition,attack2,null));
                }
            }

            if(attack.getColumn() >= 1 && board.getPiece(attack) != null && board.getPiece(attack).pieceColor == ChessGame.TeamColor.WHITE){
                if(attack.getRow() == 1){
                    possibleMove.add(new ChessMove(myPosition, attack, PieceType.QUEEN));
                    possibleMove.add(new ChessMove(myPosition, attack, PieceType.BISHOP));
                    possibleMove.add(new ChessMove(myPosition, attack, PieceType.KNIGHT));
                    possibleMove.add(new ChessMove(myPosition, attack, PieceType.ROOK));
                }else{
                    possibleMove.add(new ChessMove(myPosition,attack,null));
                }
            }
        }

        possibleMove = removeOutside(possibleMove);


        return possibleMove;

    }

    public ArrayList<ChessMove> rookMove(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> possibleMove){

        for(int i = 1; i < 8; i++){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + i, myPosition.getColumn());
            ChessMove newMove = new ChessMove(myPosition, newPosition, null);

            if(newPosition.getRow() > 8){
                break;
            }else if(board.getPiece(newPosition) != null){
                if (board.getPiece(newPosition).pieceColor != board.getPiece(myPosition).pieceColor) {
                    possibleMove.add(newMove);
                }
                break;
            }else{
                possibleMove.add(newMove);
            }
        }

        for(int i = 1; i < 8; i++){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() - i, myPosition.getColumn());
            ChessMove newMove = new ChessMove(myPosition, newPosition, null);

            if(newPosition.getRow() < 1){
                break;
            }else if(board.getPiece(newPosition) != null){
                if (board.getPiece(newPosition).pieceColor != board.getPiece(myPosition).pieceColor) {
                    possibleMove.add(newMove);
                }
                break;
            }else{
                possibleMove.add(newMove);
            }
        }

        for(int i = 1; i < 8; i++){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn() + i);
            ChessMove newMove = new ChessMove(myPosition, newPosition, null);

            if(newPosition.getColumn() > 8){
                break;
            }else if(board.getPiece(newPosition) != null){
                if (board.getPiece(newPosition).pieceColor != board.getPiece(myPosition).pieceColor) {
                    possibleMove.add(newMove);
                }
                break;
            }else{
                possibleMove.add(newMove);
            }
        }

        for(int i = 1; i < 8; i++){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn() - i);
            ChessMove newMove = new ChessMove(myPosition, newPosition, null);

            if(newPosition.getColumn() < 1){
                break;
            }else if(board.getPiece(newPosition) != null){
                if (board.getPiece(newPosition).pieceColor != board.getPiece(myPosition).pieceColor) {
                    possibleMove.add(newMove);
                }
                break;
            }else{
                possibleMove.add(newMove);
            }
        }

        return possibleMove;
    }

    public ArrayList<ChessMove> bishopMove(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> possibleMove){

        for(int i = 1; i < 8; i++){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + i, myPosition.getColumn() + i);
            ChessMove newMove = new ChessMove(myPosition, newPosition, null);

            if(newPosition.getRow() > 8 || newPosition.getColumn() > 8){
                break;
            }else if(board.getPiece(newPosition) != null){
                if (board.getPiece(newPosition).pieceColor != board.getPiece(myPosition).pieceColor) {
                    possibleMove.add(newMove);
                }
                break;
            }else{
                possibleMove.add(newMove);
            }
        }

        for(int i = 1; i < 8; i++){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() - i, myPosition.getColumn() - i);
            ChessMove newMove = new ChessMove(myPosition, newPosition, null);

            if(newPosition.getRow() < 1 || newPosition.getColumn() < 1){
                break;
            }else if(board.getPiece(newPosition) != null){
                if (board.getPiece(newPosition).pieceColor != board.getPiece(myPosition).pieceColor) {
                    possibleMove.add(newMove);
                }
                break;
            }else{
                possibleMove.add(newMove);
            }
        }

        for(int i = 1; i < 8; i++){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() - i, myPosition.getColumn() + i);
            ChessMove newMove = new ChessMove(myPosition, newPosition, null);

            if(newPosition.getColumn() > 8 || newPosition.getRow() < 1){
                break;
            }else if(board.getPiece(newPosition) != null){
                if (board.getPiece(newPosition).pieceColor != board.getPiece(myPosition).pieceColor) {
                    possibleMove.add(newMove);
                }
                break;
            }else{
                possibleMove.add(newMove);
            }
        }

        for(int i = 1; i < 8; i++){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + i, myPosition.getColumn() - i);
            ChessMove newMove = new ChessMove(myPosition, newPosition, null);

            if(newPosition.getColumn() < 1 || newPosition.getRow() > 8){
                break;
            }else if(board.getPiece(newPosition) != null){
                if (board.getPiece(newPosition).pieceColor != board.getPiece(myPosition).pieceColor) {
                    possibleMove.add(newMove);
                }
                break;
            }else{
                possibleMove.add(newMove);
            }
        }




        return possibleMove;
    }

    public ArrayList<ChessMove> queenMove(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> possibleMove){

        ArrayList<ChessMove> possibleMove2 = new ArrayList<>();

        possibleMove = rookMove(board, myPosition, possibleMove);
        possibleMove2 = bishopMove(board, myPosition, possibleMove2);

        possibleMove.addAll(possibleMove2);

        return possibleMove;
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
