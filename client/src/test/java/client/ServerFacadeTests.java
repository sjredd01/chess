package client;

import chess.ChessGame;
import dataaccess.*;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;


import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {
    private static final String serverUrl = "http://localhost:8080";
    private static Server server;
    private static final ServerFacade facade = new ServerFacade(serverUrl);
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

    static UserDAO userDAO;

    static {
        try {
            userDAO = new MySQLUserDAO();
        } catch (ResponseException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static ChessGame game = new ChessGame();
    static AuthData authData = new AuthData("testAuthToken", "testUsername");
    static GameData gameData = new GameData(123, "testWhite", "testBlack", "testGameName", game);
    static UserData userData = new UserData("testUsername", "testPassword", "testEmail");


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clear() throws ResponseException {
        gameDAO.clear();
        authDAO.clear();
        userDAO.clear();
    }


    @Test
    public void sampleTest() {
        assertTrue(true);
    }

    @Test
    void register() throws ResponseException, DataAccessException {
        facade.registerUser(userData);

        assertEquals(userDAO.getUser(userData.username()).username(), userData.username());
    }

    @Test
    void registerNegative(){
        assertThrows(RuntimeException.class, () -> facade.registerUser(new UserData(null, "test", "email")));
    }

    @Test
    void login(){
        facade.registerUser(userData);

        assertTrue(facade.logIn(userData));

    }

    @Test
    void loginNegative(){
        facade.registerUser(userData);

        UserData fakeData = new UserData(userData.username(), "fake", null);

        assertThrows(RuntimeException.class, () -> facade.logIn(fakeData));

    }

    @Test
    void logout(){
        facade.registerUser(userData);
        facade.logIn(userData);

        assertTrue(facade.logOut());

    }

    @Test
    void logoutNegative(){
        facade.registerUser(userData);
        facade.logOut();

        assertThrows(RuntimeException.class, facade::logOut);
    }

    @Test
    void createGame() throws ResponseException, DataAccessException {
        facade.registerUser(userData);
        facade.logIn(userData);
        int gameID = facade.createGame(gameData);

        assertEquals(gameID, gameDAO.getGame(gameID).gameID());

    }

    @Test
    void createGameNegative(){
        assertThrows(RuntimeException.class,() -> facade.createGame(gameData));
    }

    @Test
    void listGame(){
        facade.registerUser(userData);
        facade.logIn(userData);
        facade.createGame(gameData);

        assertEquals(1,facade.listGames().size());
    }

    @Test
    void listGameNegative(){
        assertThrows(RuntimeException.class, () -> facade.createGame(gameData));
    }

    @Test
    void joinGame() throws ResponseException, DataAccessException {
        facade.registerUser(userData);
        facade.logIn(userData);
        int gameID = facade.createGame(gameData);

        facade.joinGame(gameID, "WHITE");

        assertEquals(gameDAO.getGame(gameID).whiteUsername(), userData.username());

    }

    @Test
    void joinGameNegative(){
        facade.registerUser(userData);
        facade.logIn(userData);
        int gameID = facade.createGame(gameData);

        assertThrows(RuntimeException.class, () -> facade.joinGame(gameID, "GREEN"));

    }

}
