package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class SQLTests {


    static GameDAO gameDAO;

    static {
        try {
            gameDAO = new MySQLGameDAO();
        } catch (ResponseException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static AuthDAO authDAO;

    static {
        try {
            authDAO = new MySQLAuthDAO();
        } catch (ResponseException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static UserDAO userDAO = new MySQLUserDAO();
    static ChessGame game = new ChessGame();
    static AuthData authData = new AuthData("testAuthToken", "testUsername");
    static GameData gameData = new GameData(123, "testWhite", "testBlack", "testGameName", game);
    static UserData userData = new UserData("testUsername", "testPassword", "testEmail");


    @Test
    void testAuthClear() throws ResponseException {
        authDAO.createAuth(authData);

        authDAO.clear();

        assertThrows(DataAccessException.class, () -> authDAO.getAuth(authData.authToken()));
    }

    @Test
    void testAuthCreate() throws ResponseException, DataAccessException {
        authDAO.createAuth(authData);
        assertNotNull(authDAO.getAuth(authData.authToken()).authToken());
        assertEquals(authData.authToken(), authDAO.getAuth(authData.authToken()).authToken());
        assertEquals(authData.username(), authDAO.getAuth(authData.authToken()).username());

    }

    @Test
    void testAuthCreateNegative() throws ResponseException {
        authDAO.createAuth(authData);
        AuthData authData2 = new AuthData(null, "1111");
        assertThrows(ResponseException.class, () -> authDAO.createAuth(authData2));

    }

    @Test
    void testAuthGet() throws ResponseException, DataAccessException {
        authDAO.createAuth(authData);
        assertEquals(authData.authToken(), authDAO.getAuth(authData.authToken()).authToken());
    }

    @Test
    void testAuthGetNegative() throws ResponseException {
        AuthData authData2 = new AuthData(null, authData.username());
        authDAO.createAuth(authData);
        assertThrows(DataAccessException.class, () -> authDAO.getAuth(authData2.authToken()));
    }

    @Test
    void testAuthDelete() throws ResponseException, DataAccessException {
        authDAO.createAuth(authData);
        authDAO.deleteAuth(authData.authToken());
        assertThrows(DataAccessException.class, () -> authDAO.getAuth(authData.authToken()));
    }

    @Test
    void testAuthDeleteNegative() throws ResponseException {
        authDAO.createAuth(authData);
        assertThrows(ResponseException.class, () -> authDAO.deleteAuth(null));
    }

    @Test
    void testUserClear() throws ResponseException, SQLException, DataAccessException {
        userDAO.createUser(userData);

        userDAO.clear();

        assertThrows(DataAccessException.class, () -> userDAO.getUser(userData.username()));
    }

    @Test
    void testUserCreate() throws ResponseException, DataAccessException, SQLException {
        userDAO.createUser(userData);
        assertNotNull(userDAO.getUser(userData.username()));
        assertEquals(userData.username(), userDAO.getUser(userData.username()).username());
        assertEquals(userData.email(), userDAO.getUser(userData.username()).email());

    }

    @Test
    void testUserNegative() throws ResponseException, DataAccessException, SQLException {
        userDAO.createUser(userData);
        UserData userData2 = new UserData(null, "1111", "email");

        assertThrows(DataAccessException.class, () -> userDAO.getUser(userData2.username()));

    }

    @Test
    void testUserGet() throws ResponseException, DataAccessException, SQLException {
        userDAO.createUser(userData);
        assertEquals(userData.username(), userDAO.getUser(userData.username()).username());
    }

    @Test
    void testUserGetNegative() throws ResponseException, SQLException, DataAccessException {
        UserData userData2 = new UserData(null, userData.username(), userData.email());
        userDAO.createUser(userData);
        assertThrows(DataAccessException.class, () -> userDAO.getUser(userData2.username()));
    }

    @Test
    void testCheckUser() throws ResponseException, SQLException, DataAccessException {
        userDAO.createUser(userData);

        assertTrue(userDAO.checkUser(userData.username(), userData.password()));
    }

    @Test
    void testCheckUserNegative() throws ResponseException, SQLException, DataAccessException {
        userDAO.createUser(userData);

        assertFalse(userDAO.checkUser(userData.username(), "badPassword"));
    }

    @Test
    void testGameClear() throws ResponseException, DataAccessException {
        gameDAO.createGame(gameData);
        gameDAO.clear();
        assertThrows(DataAccessException.class, () -> gameDAO.getGame(gameData.gameID()));
    }

    @Test
    void testCreateGame() throws DataAccessException, ResponseException {
        gameDAO.createGame(gameData);
        assertNotNull(gameDAO.getGame(gameData.gameID()));
        assertEquals(gameData.whiteUsername(), gameDAO.getGame(gameData.gameID()).whiteUsername());
        assertEquals(gameData.blackUsername(), gameDAO.getGame(gameData.gameID()).blackUsername());
    }

    @Test
    void testCreateGameNegative() throws ResponseException, DataAccessException {
        gameDAO.createGame(gameData);
        GameData gameData1 = new GameData(1, "testWhite2", "testBlack2", "testGameName2", null);
        assertThrows(DataAccessException.class, () -> gameDAO.getGame(12));
    }


    @Test
    void testGetGame() throws ResponseException, DataAccessException {
        gameDAO.createGame(gameData);
        assertEquals(gameData.gameID(), gameDAO.getGame(gameData.gameID()).gameID());
    }

    @Test
    void testGetGameNegative() throws ResponseException, DataAccessException {
        gameDAO.createGame(gameData);
        assertThrows(DataAccessException.class, ()-> gameDAO.getGame(1111));
    }

    @Test
    void testGameExists() throws ResponseException, DataAccessException {
        gameDAO.createGame(gameData);
        assertTrue(gameDAO.gameExists(gameData.gameID()));
    }

    @Test
    void testGameExistsNegative() throws ResponseException, DataAccessException {
        assertThrows(DataAccessException.class, () -> gameDAO.gameExists(11111));
    }

    @Test
    void testGameUpdate() throws ResponseException, DataAccessException {
        gameDAO.createGame(gameData);
        ChessGame newGame = new ChessGame();
        GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), newGame);

        gameDAO.updateGame(newGameData);

        assertEquals(newGame.getBoard(), gameDAO.getGame(gameData.gameID()).game().getBoard());

    }

    @Test
    void testGameUpdateNegative() throws ResponseException, DataAccessException {
        gameDAO.createGame(gameData);

        GameData newGameData = null;

        assertThrows(NullPointerException.class, () -> gameDAO.updateGame(newGameData));

    }



}
