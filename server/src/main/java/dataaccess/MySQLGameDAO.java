package dataaccess;

import model.GameData;

import java.util.HashSet;

public class MySQLGameDAO implements GameDAO{





    @Override
    public HashSet<GameData> listGames() {
        return null;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {

    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException {
        return null;
    }

    @Override
    public boolean gameExists(int gameId) {
        return false;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }

    @Override
    public void clear() {

    }
}
