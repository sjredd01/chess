package dataaccess;
import chess.ChessGame;
import model.UserData;
import model.AuthData;
import model.GameData;

import java.util.Collection;


public interface DataAccess {

    Collection<GameData> listGames();
    GameData getGame(int gameID) throws DataAccessException;
    void updateGameData(GameData game) throws DataAccessException;
    void createGame(GameData game) throws DataAccessException;
    boolean gameExists(int gameID);
    boolean checkIfColorInGame(String color);
    void deleteAllGames();


    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void deleteAllUsers();




    void createAuth(AuthData authData);
    void deleteAuth(String authToken);
    AuthData getAuth(String authToken) throws DataAccessException;
    boolean checkAuth(String authToken);
    void deleteAllAuthTokens();

}
