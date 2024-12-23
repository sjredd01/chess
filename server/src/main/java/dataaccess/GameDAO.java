package dataaccess;

import exception.ResponseException;
import model.GameData;

import java.util.HashSet;

public interface GameDAO {

    HashSet<GameData> listGames() throws ResponseException;
    void createGame(GameData game) throws DataAccessException, ResponseException;
    GameData getGame(int gameId) throws DataAccessException, ResponseException;
    boolean gameExists(int gameId) throws ResponseException, DataAccessException;
    void updateGame(GameData game) throws DataAccessException, ResponseException;
    void clear() throws ResponseException;
}
