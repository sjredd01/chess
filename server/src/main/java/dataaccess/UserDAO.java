package dataaccess;

import model.UserData;

public interface UserDAO {
    public void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    boolean checkUser(String username, String password) throws DataAccessException;
    void clear();
}
