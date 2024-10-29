package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import exception.ResponseException;

public class AdminService {

    GameDAO gameDAO;
    AuthDAO authDAO;
    UserDAO userDAO;

    public AdminService(GameDAO gameDAO, AuthDAO authDAO, UserDAO userDAO ){
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
        this.userDAO = userDAO;
    }

    public void clear() throws ResponseException {
        gameDAO.clear();
        userDAO.clear();
        authDAO.clear();
    }


}
