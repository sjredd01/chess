package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import service.AdminService;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {

    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;

    static AdminService adminService;
    static GameService gameService;
    static UserService userService;

    public Server(){
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        adminService = new AdminService(gameDAO, authDAO, userDAO);
        gameService = new GameService(gameDAO, authDAO, userDAO);
        userService = new UserService(gameDAO, authDAO, userDAO);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", this::clear);
        Spark.post("/user", this::registerNewUser);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);




        //This line initializes the server and can be removed once you have a functioning endpoint 
        //Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object joinGame(Request request, Response response) throws DataAccessException {
        String authToken = request.headers("authorization");
        var gameID = new Gson().fromJson(request.body(), GameData.class).gameID();
        var playerColor = new Gson().fromJson(request.body(), String.class);

        gameService.joinGame(playerColor, gameID, authToken);

        response.status(200);
        return "{}";
    }

    private Object listGames(Request request, Response response) {
        response.status(200);
        return "{ \"gameID\": ";
    }

    private Object logoutUser(Request request, Response response) throws DataAccessException {
        String authToken = request.headers("authorization");
        try{
            userService.logoutUser(authToken);
            response.status(200);
            return "{}";
        } catch (DataAccessException e) {
            response.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        }
    }

    private Object loginUser(Request request, Response response) {
        var username = new Gson().fromJson(request.body(), UserData.class).username();
        var password = new Gson().fromJson(request.body(), UserData.class).password();

        try {
            var authToken = userService.loginUser(username, password);
            response.status(200);
            return "{ \"username\": " + username + ", \"authToken\": " + authToken + "}";
        } catch (DataAccessException e) {
            response.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        }

    }

    private Object registerNewUser(Request request, Response response) throws DataAccessException {

        var username = new Gson().fromJson(request.body(), UserData.class).username();
        var password = new Gson().fromJson(request.body(), UserData.class).password();
        var email = new Gson().fromJson(request.body(), UserData.class).email();

        if(username == null || password == null || email == null){
            response.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }

       try{
           var authToken = userService.createNewUser(username, password, email).authToken();
           response.status(200);
           return "{ \"username\": " + username + ", \"authToken\": " + authToken + "}";
       } catch (RuntimeException e) {
           response.status(403);
           return "{ \"message\": \"Error: already taken\" }";
       } 


    }

    private Object createGame(Request request, Response response) throws UnauthorizedException, BadRequestException, DataAccessException {
        if(!request.body().contains("\"gameName\":")){
            throw new BadRequestException("No gameName provided");
        }

        GameData gameData = new Gson().fromJson(request.body(), GameData.class);

        String authToken = request.headers("authorization");
        int gameID = gameService.createGame(gameData.gameName(), authToken);

        response.status(200);
        return "{ \"gameID\": " + gameID + "}";

    }

    private Object clear(Request request, Response response) {
        adminService.clear();

        response.status(200);
        return "{}";
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
