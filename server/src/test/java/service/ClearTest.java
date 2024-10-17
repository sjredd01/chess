package service;
import chess.ChessGame;
import dataaccess.*;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import service.AdminService;
import model.AuthData;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ClearTest {
    static GameDAO gameDAO = new MemoryGameDAO();
    static AuthDAO authDAO = new MemoryAuthDAO();
    static UserDAO userDAO = new MemoryUserDAO();
    static ChessGame game = new ChessGame();
    static AuthData authData = new AuthData("testAuthToken", "testUsername");;
    static GameData gameData = new GameData(123, "testWhite", "testBlack", "testGameName", game);;
    static UserData userData = new UserData("testUsername", "testPassword", "testEmail");;



    static final AdminService service = new AdminService(gameDAO, authDAO, userDAO);

    @BeforeEach
    void start(){
        gameDAO.clear();
        authDAO.clear();
        userDAO.clear();
    }

    @Test
    void testClear() throws DataAccessException {
        gameDAO.createGame(gameData);
        authDAO.createAuth(authData);
        userDAO.createUser(userData);

        service.clear();

        assertThrows(DataAccessException.class, () -> gameDAO.getGame(gameData.gameID()));
        assertThrows(DataAccessException.class, () -> authDAO.getAuth(authData.authToken()));
        assertThrows(DataAccessException.class, () -> userDAO.getUser(userData.username()));
    }
}
