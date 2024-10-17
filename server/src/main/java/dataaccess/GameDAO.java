package dataaccess;

import model.GameData;

import java.util.HashSet;

public interface GameDAO {

    HashSet<GameData> listGames();
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameId) throws DataAccessException;
    boolean gameExists(int gameId);
    void updateGame(GameData game) throws DataAccessException;
    void clear();
}
