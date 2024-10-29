package dataaccess;

import exception.ResponseException;
import model.AuthData;

public interface AuthDAO {

    public void createAuth(AuthData authData) throws ResponseException;
    AuthData getAuth(String authToken) throws DataAccessException, ResponseException;

    void deleteAuth(String authToken) throws ResponseException;


    void clear() throws ResponseException;
}
