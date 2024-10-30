package dataaccess;

import exception.ResponseException;
import model.UserData;

import java.sql.SQLException;

public interface UserDAO {
    public void createUser(UserData user) throws DataAccessException, ResponseException, SQLException;
    UserData getUser(String username) throws DataAccessException, ResponseException;
    boolean checkUser(String username, String password) throws DataAccessException, ResponseException;
    void clear() throws ResponseException;
}
