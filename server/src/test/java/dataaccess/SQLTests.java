package dataaccess;

import chess.ChessGame;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SQLTests {

    static GameDAO gameDAO = new MemoryGameDAO();
    static AuthDAO authDAO;

    static {
        try {
            authDAO = new MySQLAuthDAO();
        } catch (ResponseException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static UserDAO userDAO = new MemoryUserDAO();
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

}
