package chess;

import java.util.*;

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
        List<ChessMove> possibleMoves1 = new ArrayList<>();

        if(test.getPieceType() == PieceType.ROOK){
            possibleMoves = rookMoves(board, myPosition);
        }if(test.getPieceType() == PieceType.KNIGHT){
            possibleMoves = knightMoves(board, myPosition);
        }if(test.getPieceType() == PieceType.KING){
            possibleMoves = kingMoves(board, myPosition);
        }if(test.getPieceType() == PieceType.PAWN){
            possibleMoves = pawnMoves(board, myPosition);
        }
        if(test.getPieceType() == PieceType.BISHOP){
            possibleMoves = bishopMoves(board, myPosition);
        }
        if(test.getPieceType() == PieceType.QUEEN){
            possibleMoves = bishopMoves(board, myPosition);
            possibleMoves1 = rookMoves(board,myPosition);
            possibleMoves.addAll(possibleMoves1);
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

        possibleMoves = removeMoves(board, myPosition, possibleMoves);

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

        while(position < 8){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + position, myPosition.getColumn());
            ChessMove newOne = new ChessMove(myPosition, newPosition, null);
            if((newOne.getEndPosition().getRow() > 8)){
                break;
            }

            possibleMoves.add(newOne);
            position ++;
        }

        while(backPosition < 7){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() - backPosition, myPosition.getColumn());
            ChessMove newOne = new ChessMove(myPosition, newPosition, null);
            if((newOne.getEndPosition().getRow() <= 0)){
                break;
            }
            possibleMoves.add(newOne);
            backPosition ++;
        }

        while(rightPosition < 7){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn() + rightPosition);
            ChessMove newOne = new ChessMove(myPosition, newPosition, null);
            if((newOne.getEndPosition().getColumn() > 8)){
                break;
            }
            possibleMoves.add(newOne);
            rightPosition ++;
        }

        while(leftPosition < 7){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn() - leftPosition);
            ChessMove newOne = new ChessMove(myPosition, newPosition, null);
            if((newOne.getEndPosition().getColumn() <= 0)){
                break;
            }
            possibleMoves.add(newOne);
            leftPosition ++;
        }

      possibleMoves = removeRookMoves(board, myPosition, possibleMoves);

        return possibleMoves;
    }

    public List<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition){
        List<ChessMove> possibleMoves = new ArrayList<>();
        ChessMove newOne = knightJumps(myPosition, 1, 0);
        ChessMove newOne1 = knightJumps(myPosition, -1, 0);
        ChessMove newOne2 = knightJumps(myPosition, 0, 1);
        ChessMove newOne3 = knightJumps(myPosition, 0, -1);
        ChessMove newOne4 = knightJumps(myPosition, 1, 1);
        ChessMove newOne5 = knightJumps(myPosition, -1, 1);
        ChessMove newOne6 = knightJumps(myPosition, 1, -1);
        ChessMove newOne7 = knightJumps(myPosition, -1, -1);

        possibleMoves.add(newOne);
        possibleMoves.add(newOne1);
        possibleMoves.add(newOne2);
        possibleMoves.add(newOne3);
        possibleMoves.add(newOne4);
        possibleMoves.add(newOne5);
        possibleMoves.add(newOne6);
        possibleMoves.add(newOne7);

        possibleMoves = removeMoves(board, myPosition, possibleMoves);

        return possibleMoves;
    }

    public List<ChessMove> removeMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> possibleMoves){
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

    private List<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();

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
        possibleMoves = removeOffBoard(board, possibleMoves);

        possibleMoves = removeBishopMoves(board, myPosition, possibleMoves);

        return possibleMoves;

    }

    public List<ChessMove> removeRookMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> possibleMoves){


        for(int i = 0; i < possibleMoves.size();){
            if(board.getPiece(possibleMoves.get(i).getEndPosition()) != null) {
                if(myPosition.getRow() < possibleMoves.get(i).getEndPosition().getRow()){
                    for(int k = i; k < possibleMoves.size();){
                        if(possibleMoves.get(i).getEndPosition().getRow() < possibleMoves.get(k).getEndPosition().getRow()){
                            possibleMoves.remove(k);
                        }else{
                            k++;
                        }
                    }
                } else if (myPosition.getRow() > possibleMoves.get(i).getEndPosition().getRow()) {
                    for(int k = i; k < possibleMoves.size();){
                        if(possibleMoves.get(i).getEndPosition().getRow() > possibleMoves.get(k).getEndPosition().getRow()){
                            possibleMoves.remove(k);
                        }else{
                            k++;
                        }
                    }
                }else if(myPosition.getColumn() < possibleMoves.get(i).getEndPosition().getColumn()){
                    for(int k = i; k < possibleMoves.size();){
                        if(possibleMoves.get(i).getEndPosition().getColumn() < possibleMoves.get(k).getEndPosition().getColumn()){
                            possibleMoves.remove(k);
                        }else{
                            k++;
                        }
                    }
                }else if(myPosition.getColumn() > possibleMoves.get(i).getEndPosition().getColumn()){
                    for(int k = i; k < possibleMoves.size();){
                        if(possibleMoves.get(i).getEndPosition().getColumn() > possibleMoves.get(k).getEndPosition().getColumn()){
                            possibleMoves.remove(k);
                        }else{
                            k++;
                        }
                    }
                }
            }
            i++;
        }

        for(int i = 0; i < possibleMoves.size();){
            if(board.getPiece(possibleMoves.get(i).getEndPosition()) != null){
                if(board.getPiece(possibleMoves.get(i).getEndPosition()).getTeamColor() == board.getPiece(possibleMoves.get(i).getStartPosition()).getTeamColor()){
                    possibleMoves.remove(i);
                }else{
                   i++;
                }
            }else{
                i++;
            }
        }


        return possibleMoves;
    }

    public List<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition){
        List<ChessMove> possibleMoves = new ArrayList<>();

        if(board.getPiece(myPosition).pieceColor == ChessGame.TeamColor.WHITE) {
            possibleMoves = pawnUp(board, myPosition);
        }else{
            possibleMoves = pawnDown(board, myPosition);
        }

        return possibleMoves;
    }

    public List<ChessMove> pawnUp(ChessBoard board, ChessPosition myPosition){
        List<ChessMove> possibleMoves = new ArrayList<>();

        ChessPosition newPosition = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn());
        ChessPosition newPosition1 = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
        ChessPosition attackPosition1 = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1);
        ChessPosition attackPosition2 = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1);

        if(attackPosition1.getColumn() < 8 && board.getPiece(attackPosition1) != null && board.getPiece(attackPosition1).pieceColor == ChessGame.TeamColor.BLACK){
            ChessMove newOne = knightJumps(myPosition, 1, 1);
            if(newOne.getEndPosition().getRow() == 1){
                ChessMove newOne1 = promotePawn(newOne, PieceType.BISHOP);
                ChessMove newOne2 = promotePawn(newOne, PieceType.QUEEN);
                ChessMove newOne3 = promotePawn(newOne, PieceType.ROOK);
                ChessMove newOne4 =  promotePawn(newOne, PieceType.KNIGHT);
                possibleMoves.add(newOne1);
                possibleMoves.add(newOne2);
                possibleMoves.add(newOne3);
                possibleMoves.add(newOne4);
            }else {
                possibleMoves.add(newOne);
            }
        }

        if(attackPosition2.getColumn() > 0 && board.getPiece(attackPosition2) != null && board.getPiece(attackPosition2).pieceColor == ChessGame.TeamColor.BLACK){
            ChessMove newOne = knightJumps(myPosition, 1, -1);
            if(newOne.getEndPosition().getRow() == 1){
                ChessMove newOne1 = promotePawn(newOne, PieceType.BISHOP);
                ChessMove newOne2 = promotePawn(newOne, PieceType.QUEEN);
                ChessMove newOne3 = promotePawn(newOne, PieceType.ROOK);
                ChessMove newOne4 =  promotePawn(newOne, PieceType.KNIGHT);
                possibleMoves.add(newOne1);
                possibleMoves.add(newOne2);
                possibleMoves.add(newOne3);
                possibleMoves.add(newOne4);
            }else {
                possibleMoves.add(newOne);
            }
        }

        if(board.getPiece(newPosition1) == null){
            ChessMove newOne = knightJumps(myPosition, 1, 0);
            if(newOne.getEndPosition().getRow() == 8){
                ChessMove newOne1 = promotePawn(newOne, PieceType.BISHOP);
                ChessMove newOne2 = promotePawn(newOne, PieceType.QUEEN);
                ChessMove newOne3 = promotePawn(newOne, PieceType.ROOK);
                ChessMove newOne4 =  promotePawn(newOne, PieceType.KNIGHT);
                possibleMoves.add(newOne1);
                possibleMoves.add(newOne2);
                possibleMoves.add(newOne3);
                possibleMoves.add(newOne4);
            }else{
                possibleMoves.add(newOne);
            }
            if(myPosition.getRow() == 2 && (board.getPiece(newPosition) == null )){
                ChessMove newOne1 = knightJumps(myPosition, 2, 0);
                possibleMoves.add(newOne1);
            }
        }

        return possibleMoves;
    }

    public List<ChessMove> pawnDown(ChessBoard board, ChessPosition myPosition){
        List<ChessMove> possibleMoves = new ArrayList<>();

        ChessPosition newPosition = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn());
        ChessPosition newPosition1 = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
        ChessPosition attackPosition1 = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);
        ChessPosition attackPosition2 = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1);

        if(attackPosition1.getColumn() > 0 && board.getPiece(attackPosition1) != null && board.getPiece(attackPosition1).pieceColor == ChessGame.TeamColor.WHITE){
            ChessMove newOne = knightJumps(myPosition, -1, -1);
            if(newOne.getEndPosition().getRow() == 1){
                ChessMove newOne1 = promotePawn(newOne, PieceType.BISHOP);
                ChessMove newOne2 = promotePawn(newOne, PieceType.QUEEN);
                ChessMove newOne3 = promotePawn(newOne, PieceType.ROOK);
                ChessMove newOne4 =  promotePawn(newOne, PieceType.KNIGHT);
                possibleMoves.add(newOne1);
                possibleMoves.add(newOne2);
                possibleMoves.add(newOne3);
                possibleMoves.add(newOne4);
            }else {
                possibleMoves.add(newOne);
            }

        }

        if(attackPosition2.getColumn() < 8 && board.getPiece(attackPosition2) != null && board.getPiece(attackPosition2).pieceColor == ChessGame.TeamColor.WHITE){
            ChessMove newOne = knightJumps(myPosition, -1, +1);
            if(newOne.getEndPosition().getRow() == 1){
                ChessMove newOne1 = promotePawn(newOne, PieceType.BISHOP);
                ChessMove newOne2 = promotePawn(newOne, PieceType.QUEEN);
                ChessMove newOne3 = promotePawn(newOne, PieceType.ROOK);
                ChessMove newOne4 =  promotePawn(newOne, PieceType.KNIGHT);
                possibleMoves.add(newOne1);
                possibleMoves.add(newOne2);
                possibleMoves.add(newOne3);
                possibleMoves.add(newOne4);
            }else {
                possibleMoves.add(newOne);
            }
        }

        if(board.getPiece(newPosition1) == null){
            ChessMove newOne = knightJumps(myPosition, -1, 0);
            if(newOne.getEndPosition().getRow() == 1){
                ChessMove newOne1 = promotePawn(newOne, PieceType.BISHOP);
                ChessMove newOne2 = promotePawn(newOne, PieceType.QUEEN);
                ChessMove newOne3 = promotePawn(newOne, PieceType.ROOK);
                ChessMove newOne4 =  promotePawn(newOne, PieceType.KNIGHT);
                possibleMoves.add(newOne1);
                possibleMoves.add(newOne2);
                possibleMoves.add(newOne3);
                possibleMoves.add(newOne4);
            }else {
                possibleMoves.add(newOne);
            }
            if(myPosition.getRow() == 7 && (board.getPiece(newPosition) == null )){
                ChessMove newOne1 = knightJumps(myPosition, -2, 0);
                possibleMoves.add(newOne1);
            }
        }

        return possibleMoves;
    }

    public ChessMove promotePawn(ChessMove endOfBoard, ChessPiece.PieceType promoteTo){

        return new ChessMove(endOfBoard.getStartPosition(), endOfBoard.getEndPosition(), promoteTo);
    }

    public List<ChessMove> removeBishopMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> possibleMoves){
        int myRow = myPosition.getRow();
        int myColumn = myPosition.getColumn();

        for(int i = 0; i < possibleMoves.size();) {
            int checkRow = possibleMoves.get(i).getEndPosition().getRow();
            int checkColumn = possibleMoves.get(i).getEndPosition().getColumn();

            if(board.getPiece(possibleMoves.get(i).getEndPosition()) != null){
                if((myRow < checkRow) && (myColumn < checkColumn)){
                    for(int k = i; k < possibleMoves.size();){
                        int largerRow = possibleMoves.get(k).getEndPosition().getRow();
                        int largerColumn = possibleMoves.get(k).getEndPosition().getColumn();
                        if((checkRow < largerRow) && (checkColumn < largerColumn)){
                            possibleMoves.remove(k);
                        }else{
                            k++;
                        }
                    }
                }else if((myRow < checkRow) && (myColumn > checkColumn)){
                        for(int k = i; k < possibleMoves.size();){
                            int largerRow = possibleMoves.get(k).getEndPosition().getRow();
                            int largerColumn = possibleMoves.get(k).getEndPosition().getColumn();
                            if((checkRow < largerRow) && (checkColumn > largerColumn)){
                                possibleMoves.remove(k);
                            }else{
                                k++;
                            }
                        }

                }else if ((myRow > checkRow) && (myColumn > checkColumn)) {
                        for (int k = i; k < possibleMoves.size(); ) {
                            int largerRow = possibleMoves.get(k).getEndPosition().getRow();
                            int largerColumn = possibleMoves.get(k).getEndPosition().getColumn();
                            if ((checkRow > largerRow) && (checkColumn > largerColumn)) {
                                possibleMoves.remove(k);
                            } else {
                                k++;
                            }
                        }
                }else if((myRow > checkRow) && (myColumn < checkColumn)) {
                    for (int k = i; k < possibleMoves.size(); ) {
                        int largerRow = possibleMoves.get(k).getEndPosition().getRow();
                        int largerColumn = possibleMoves.get(k).getEndPosition().getColumn();
                        if ((checkRow > largerRow) && (checkColumn < largerColumn)) {
                            possibleMoves.remove(k);
                        } else {
                            k++;
                        }
                    }
                }
                }
            i++;
        }

        for(int i = 0; i < possibleMoves.size();){
            if(board.getPiece(possibleMoves.get(i).getEndPosition()) != null){
                if(board.getPiece(possibleMoves.get(i).getEndPosition()).getTeamColor() == board.getPiece(possibleMoves.get(i).getStartPosition()).getTeamColor()){
                    possibleMoves.remove(i);
                }else{
                    i++;
                }
            }else{
                i++;
            }
        }
        return possibleMoves;
    }

    public List<ChessMove> removeOffBoard(ChessBoard board, List<ChessMove> possibleMoves){
        for(int i = 0; i < possibleMoves.size();){
            if((possibleMoves.get(i).getEndPosition().getRow() > 8) || (possibleMoves.get(i).getEndPosition().getRow() < 1) || (possibleMoves.get(i).getEndPosition().getColumn() > 8) || (possibleMoves.get(i).getEndPosition().getColumn() < 1)){
                possibleMoves.remove(i);
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
