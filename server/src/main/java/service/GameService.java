package service;

import chess.ChessBoard;
import chess.ChessGame;
import dataaccess.*;
import model.GameData;

import java.util.HashSet;
import java.util.Random;

public class GameService extends AdminService{
    public GameService(GameDAO gameDAO, AuthDAO authDAO, UserDAO userDAO) {
        super(gameDAO, authDAO, userDAO);
    }

    public int createGame(String gameName, String authToken) throws BadRequestException, UnauthorizedException {
        int gameID;
        Random random = new Random();

        try{
            authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }

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

    public HashSet<GameData> listGames(String authToken) throws DataAccessException {

        if(authDAO.getAuth(authToken) != null){
            return gameDAO.listGames();
        }else{
            throw new DataAccessException("Unauthorized");
        }
    }

    public void joinGame(String playerColor, int gameID, String authToken) throws DataAccessException {
        if(authDAO.getAuth(authToken) != null){
           if(gameDAO.gameExists(gameID)){
              GameData game = gameDAO.getGame(gameID);
              ChessGame gameToPlay = gameDAO.getGame(gameID).game();
              String gameName = gameDAO.getGame(gameID).gameName();
              var blackUser = game.blackUsername();
              var whiteUser = game.whiteUsername();
              String caller = authDAO.getAuth(authToken).username();

              if(playerColor.equals("black")){
                  if(game.blackUsername() != null){
                      throw new DataAccessException("Already taken");
                  }
                  GameData updatedGame = new GameData(gameID, whiteUser, caller, gameName, gameToPlay);
                  gameDAO.updateGame(updatedGame);
              }else if(playerColor.equals("white")){
                   if(game.whiteUsername() != null){
                       throw new DataAccessException("Already taken");
                   }
                   GameData updatedGame = new GameData(gameID, caller, blackUser, gameName, gameToPlay);
                   gameDAO.updateGame(updatedGame);
               }else{
                  throw new DataAccessException("Bad request");
              }

           }else{
               throw new DataAccessException("Game does not exist");
           }


        }else{
            throw new DataAccessException("Unauthorized");
        }
    }
}
