package dataaccess;

import model.UserData;

public interface UserDAO {
    public void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    boolean authenticateUser(String username, String password) throws DataAccessException;
    void clear();
}