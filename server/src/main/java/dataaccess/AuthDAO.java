package dataaccess;

import model.AuthData;

public interface AuthDAO {

    public void createAuth(AuthData authData);
    AuthData getAuth(String authToken) throws DataAccessException;

    void deleteAuth(String authToken);


    void clear();
}
