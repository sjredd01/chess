package service;

import dataaccess.*;

public class GameService extends AdminService{
    public GameService(GameDAO gameDAO, AuthDAO authDAO, UserDAO userDAO) {
        super(gameDAO, authDAO, userDAO);
    }

    public int createGame(String gameName, String authToken) throws BadRequestException, UnauthorizedException {

    }
}
