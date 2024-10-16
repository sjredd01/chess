package service;
import chess.ChessGame;
import dataaccess.*;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import model.AuthData;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearTest {
    static GameDAO gameDAO = new MemoryGameDAO();
    static AuthDAO authDAO = new MemoryAuthDAO();
    static UserDAO userDAO = new MemoryUserDAO();
    static ChessGame game = new ChessGame();
    static AuthData authData = new AuthData("testAuthToken", "testUsername");
    static GameData gameData = new GameData(123, "testWhite", "testBlack", "testGameName", game);
    static UserData userData = new UserData("testUsername", "testPassword", "testEmail");



    static final AdminService service = new AdminService(gameDAO, authDAO, userDAO);
    static final GameService gameService = new GameService(gameDAO, authDAO, userDAO);
    static final UserService userService = new UserService(gameDAO, authDAO, userDAO);

    @BeforeEach
    void start(){
        gameDAO.clear();
        authDAO.clear();
        userDAO.clear();
    }

    @Test
    void testClear() throws DataAccessException{
        gameDAO.createGame(gameData);
        authDAO.createAuth(authData);
        userDAO.createUser(userData);
        service.clear();

        assertThrows(DataAccessException.class, () -> gameDAO.getGame(gameData.gameID()));
        assertThrows(DataAccessException.class, () -> authDAO.getAuth(authData.authToken()));
        assertThrows(DataAccessException.class, () -> userDAO.getUser(userData.username()));
    }

    @Test
    void createGamePositive() throws DataAccessException{
        authDAO.createAuth(authData);
        var newGame = gameService.createGame(gameData.gameName(), authData.authToken());
        var games = gameService.listGames(authData.authToken());

        assertEquals(1, games.size());
        assertTrue(gameDAO.gameExists(newGame));

    }

    @Test
    void createGameNegative() throws UnauthorizedException{
        authDAO.createAuth(authData);
        assertThrows(UnauthorizedException.class, () -> gameService.createGame(gameData.gameName(), "wrongAuthToken"));
    }

    @Test
    void registerNewUserNegative() throws DataAccessException {
        String username = "TestUser";
        String password = "TestPassword";
        String email = "TestEmail";

        String username1 = "TestUser";
        String password1 = "TestPassword";
        String email1 = "TestEmail";

        userService.createNewUser(username,password,email);

        assertThrows(RuntimeException.class, () -> userService.createNewUser(username1, password1, email1));
    }

    @Test
    void registerNewUserPositive() throws DataAccessException {
        String username = "TestUser1";
        String password = "TestPassword";
        String email = "TestEmail";

        userService.createNewUser(username,password,email);

        assertEquals(username, userDAO.getUser(username).username());
    }

    @Test
    void loginUserPositive() throws DataAccessException{
        String username = "TestUser1";
        String password = "TestPassword";
        String email = "TestEmail";

        userService.createNewUser(username,password,email);

        var checkAuthToken = userService.loginUser(username, password);

        boolean loggedIn = authDAO.getAuth(checkAuthToken) != null;
        assertTrue(loggedIn);
    }

    @Test
    void loginUserNegative() throws DataAccessException{
        String username = "TestUser1";
        String password = "TestPassword";
        String email = "TestEmail";
        String badPassword = "WrongEmail";

        userService.createNewUser(username,password,email);

        assertThrows(DataAccessException.class, () -> userService.loginUser(username, badPassword));

    }

    @Test
    void logoutUserPositive() throws DataAccessException{
        String username = "TestUser1";
        String password = "TestPassword";
        String email = "TestEmail";

        userService.createNewUser(username,password,email);
        var userAuthToken = userService.loginUser(username, password);
        userService.logoutUser(userAuthToken);

        assertThrows(DataAccessException.class, () -> authDAO.getAuth(userAuthToken));
    }

    @Test
    void logoutUserNegative() throws DataAccessException{
        String username = "TestUser1";
        String password = "TestPassword";
        String email = "TestEmail";
        String badAuthToken = "AjdJuA";

        userService.createNewUser(username,password,email);
        userService.loginUser(username, password);

        assertThrows(DataAccessException.class, () -> userService.logoutUser(badAuthToken));

    }

    @Test
    void listGamePositive() throws DataAccessException{
        AuthData authData1 = new AuthData("testAuthToken", "testUsername5");
        GameData gameData1 = new GameData(13323, "testWhite", "testBlack", "testGameName1", game);
        GameData gameData2 = new GameData(3, null, "testBlack", "testGameName2", game);
        GameData gameData3 = new GameData(555, null, null, "testGameName3", game);

        authDAO.createAuth(authData1);

        gameDAO.createGame(gameData1);
        gameDAO.createGame(gameData2);
        gameDAO.createGame(gameData3);

        var games = gameService.listGames(authData1.authToken());

        assertEquals(3, games.size());
        assertTrue(gameDAO.gameExists(3));
        assertTrue(gameDAO.gameExists(13323));
        assertTrue(gameDAO.gameExists(555));

    }

    @Test
    void listGameNegative() throws DataAccessException{
        AuthData authData1 = new AuthData("testAuthToken", "testUsername5");
        GameData gameData1 = new GameData(13323, "testWhite", "testBlack", "testGameName1", game);
        GameData gameData2 = new GameData(3, null, "testBlack", "testGameName2", game);
        GameData gameData3 = new GameData(555, null, null, "testGameName3", game);

        authDAO.createAuth(authData1);

        gameDAO.createGame(gameData1);
        gameDAO.createGame(gameData2);
        gameDAO.createGame(gameData3);

        assertThrows(DataAccessException.class, () ->gameService.listGames("BadAuthToken"));

    }

}
