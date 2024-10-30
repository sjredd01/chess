package dataaccess;

import exception.ResponseException;
import model.GameData;

import java.util.HashSet;

public interface GameDAO {

    HashSet<GameData> listGames();
    void createGame(GameData game) throws DataAccessException, ResponseException;
    GameData getGame(int gameId) throws DataAccessException, ResponseException;
    boolean gameExists(int gameId);
    void updateGame(GameData game) throws DataAccessException;
    void clear();
}
