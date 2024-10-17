package service;

import chess.ChessBoard;
import chess.ChessGame;
import dataaccess.*;
import model.GameData;

import java.util.Random;

public class GameService extends AdminService{
    public GameService(GameDAO gameDAO, AuthDAO authDAO, UserDAO userDAO) {
        super(gameDAO, authDAO, userDAO);
    }

    public int createGame(String gameName, String authToken) throws BadRequestException, UnauthorizedException {
        int gameID;
        Random random = new Random();

//        try{
//            authDAO.getAuth(authToken);
//        } catch (DataAccessException e) {
//            throw new UnauthorizedException();
//        }

        do{
            gameID = random.nextInt(10001);
        }while(gameDAO.gameExists(gameID));

        try {
            ChessGame game = new ChessGame();
            ChessBoard board = new ChessBoard();
            board.resetBoard();
            game.setBoard(board);
            gameDAO.createGame(new GameData(gameID,  null, null, gameName, game));
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }

        return gameID;


    }
}
