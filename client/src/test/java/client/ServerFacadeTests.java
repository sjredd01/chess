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
    private static final String serverURL = "http://localhost:8080";
    private static Server server;
    private static final ServerFacade FACADE = new ServerFacade(serverURL);
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
        FACADE.registerUser(userData);

        assertEquals(userDAO1.getUser(userData.username()).username(), userData.username());
    }

    @Test
    void registerNegative(){
        assertThrows(RuntimeException.class, () -> FACADE.registerUser(new UserData(null, "test", "email")));
    }

    @Test
    void login(){
        FACADE.registerUser(userData);

        assertTrue(FACADE.logIn(userData));

    }

    @Test
    void loginNegative(){
        FACADE.registerUser(userData);

        UserData fakeData = new UserData(userData.username(), "fake", null);

        assertThrows(RuntimeException.class, () -> FACADE.logIn(fakeData));

    }

    @Test
    void logout(){
        FACADE.registerUser(userData);
        FACADE.logIn(userData);

        assertTrue(FACADE.logOut());

    }

    @Test
    void logoutNegative(){
        FACADE.registerUser(userData);
        FACADE.logOut();

        assertThrows(RuntimeException.class, FACADE::logOut);
    }

    @Test
    void createGame() throws ResponseException, DataAccessException {
        FACADE.registerUser(userData);
        FACADE.logIn(userData);
        int gameID = FACADE.createGame(gameData);

        assertEquals(gameID, gameDAO1.getGame(gameID).gameID());

    }

    @Test
    void createGameNegative(){
        assertThrows(RuntimeException.class,() -> FACADE.createGame(gameData));
    }

    @Test
    void listGame(){
        FACADE.registerUser(userData);
        FACADE.logIn(userData);
        FACADE.createGame(gameData);

        assertEquals(1, FACADE.listGames().size());
    }

    @Test
    void listGameNegative(){
        assertThrows(RuntimeException.class, () -> FACADE.createGame(gameData));
    }

    @Test
    void joinGame() throws ResponseException, DataAccessException {
        FACADE.registerUser(userData);
        FACADE.logIn(userData);
        int gameID = FACADE.createGame(gameData);

        FACADE.joinGame(gameID, "WHITE");

        assertEquals(gameDAO1.getGame(gameID).whiteUsername(), userData.username());

    }

    @Test
    void joinGameNegative(){
        FACADE.registerUser(userData);
        FACADE.logIn(userData);
        int gameID = FACADE.createGame(gameData);

        assertThrows(RuntimeException.class, () -> FACADE.joinGame(gameID, "GREEN"));

    }

}
