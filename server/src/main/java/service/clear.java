package service;

import dataaccess.DataAccess;

import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import model.GameData;

public class clear {

    private final DataAccess dataAccess;

    public clear(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void deleteAllGames() throws DataAccessException{
        dataAccess.deleteAllGames();
    }

    public void deleteAllUsers() throws DataAccessException{
        dataAccess.deleteAllUsers();
    }

    public void deleteAllAuthTokens() throws DataAccessException{
        dataAccess.deleteAllAuthTokens();
    }
}
