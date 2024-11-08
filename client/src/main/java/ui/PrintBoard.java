package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static java.lang.System.out;
import static ui.EscapeSequences.*;

public class PrintBoard {
    ChessGame game;

    public PrintBoard(ChessGame game){
        this.game = game;
    }

    public void updateGame(ChessGame game){
        this.game = game;
    }

    public void printBoard(ChessGame.TeamColor color, ChessPosition position){
        StringBuilder board = new StringBuilder();
        board.append(SET_TEXT_BOLD);

        boolean backwards = color == ChessGame.TeamColor.BLACK;
        int printCount = color == null ? 2 : 1;

        for(int i = 0; i < printCount; i++){
            board.append(startRow(backwards));

            for(int j = 8; j > 0; j--){
                int row = !backwards ? j : (j * -1) + 9;
                board.append(chessBoardRow(row, backwards));
            }

            board.append(startRow(backwards));

            if(i < printCount - 1){
                board.append("\n");
            }
        }

        board.append(RESET_TEXT_BOLD_FAINT);

        out.println(board);
    }

    private String startRow(boolean reverse){
        StringBuilder board = new StringBuilder();
        board.append(SET_BG_COLOR_BLACK);
        board.append(SET_TEXT_COLOR_MAGENTA);
        board.append(!reverse ? "    a  b  c  d  e  f  g  h    " : "    h  g  f  e  d  c  b  a    ");
        board.append(RESET_BG_COLOR);
        board.append(RESET_TEXT_COLOR);
        board.append("\n");
        return board.toString();
    }

    private String chessBoardRow(int row, boolean reverse){
        StringBuilder rows = new StringBuilder();
        rows.append(SET_BG_COLOR_BLACK);
        rows.append(SET_TEXT_COLOR_MAGENTA);
        rows.append(" %d ".formatted(row));

        for (int i = 1; i < 9; i++) {
            int column = !reverse ? i : (i * -1) + 9;
            rows.append(squaresColor(row, column));
            rows.append(pieceColor(row, column));
        }

        rows.append(SET_BG_COLOR_BLACK);
        rows.append(SET_TEXT_COLOR_MAGENTA);
        rows.append(" %d ".formatted(row));
        rows.append(RESET_BG_COLOR);
        rows.append(RESET_TEXT_COLOR);

        rows.append("\n");
        return rows.toString();
    }

    private String squaresColor(int row, int col){

        if (Math.ceilMod(row, 2) == 0) {
            if (Math.ceilMod(col, 2) == 0) {
                return SET_BG_COLOR_RED;
            } else {
                return SET_BG_COLOR_LIGHT_GREY;
            }
        } else {
            if (Math.ceilMod(col, 2) == 0) {
                return SET_BG_COLOR_LIGHT_GREY;
            } else {
                return SET_BG_COLOR_RED;
            }
        }
    }

    private String pieceColor(int row, int col){
        StringBuilder color = new StringBuilder();
        ChessPosition position = new ChessPosition(row, col);
        ChessPiece piece = game.getBoard().getPiece(position);

        if(piece != null){
            if(piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                color.append(SET_TEXT_COLOR_WHITE);
            }else{
                color.append(SET_TEXT_COLOR_BLACK);
            }

            switch (piece.getPieceType()){
                case QUEEN -> color.append(" Q ");
                case BISHOP -> color.append(" B ");
                case KNIGHT -> color.append(" N ");
                case ROOK -> color.append(" R ");
                case KING -> color.append(" K ");
                case PAWN -> color.append(" P ");

            }
        }else{
            color.append("   ");
        }

        return color.toString();

    }
}
