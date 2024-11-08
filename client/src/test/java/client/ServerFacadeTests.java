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
    private static Server server;
    private static ServerFacade facade = null;
    static GameDAO gameDAO1;

    static {
        try {
            gameDAO1 = new MySQLGameDAO();
        } catch (ResponseException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static AuthDAO authDAO1;

    static {
        try {
            authDAO1 = new MySQLAuthDAO();
        } catch (ResponseException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static UserDAO userDAO1;

    static {
        try {
            userDAO1 = new MySQLUserDAO();
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
        String serverUrl = "http://localhost:" + port;
        facade = new ServerFacade(serverUrl);

        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clear() throws ResponseException {
        gameDAO1.clear();
        authDAO1.clear();
        userDAO1.clear();
    }


    @Test
    public void sampleTest() {
        assertTrue(true);
    }

    @Test
    void register() throws ResponseException, DataAccessException {
        facade.registerUser(userData);

        assertEquals(userDAO1.getUser(userData.username()).username(), userData.username());
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

        assertEquals(gameID, gameDAO1.getGame(gameID).gameID());

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

        assertEquals(1, facade.listGames().size());
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

        assertEquals(gameDAO1.getGame(gameID).whiteUsername(), userData.username());

    }

    @Test
    void joinGameNegative(){
        facade.registerUser(userData);
        facade.logIn(userData);
        int gameID = facade.createGame(gameData);

        assertThrows(RuntimeException.class, () -> facade.joinGame(gameID, "GREEN"));

    }

}
