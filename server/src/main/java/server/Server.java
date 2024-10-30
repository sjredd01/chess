package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import dataaccess.*;
import exception.ResponseException;
import model.*;
import service.AdminService;
import service.GameService;
import service.UserService;
import spark.*;

import java.sql.SQLException;
import java.util.*;

public class Server {

    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;

    static AdminService adminService;
    static GameService gameService;
    static UserService userService;

    public Server(){
        userDAO = new MySQLUserDAO();
        try {
            authDAO = new MySQLAuthDAO();
        } catch (ResponseException | DataAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            gameDAO = new MySQLGameDAO();
        } catch (ResponseException | DataAccessException e) {
            throw new RuntimeException(e);
        }

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

        JoinGameRequest joinData = new Gson().fromJson(request.body(), JoinGameRequest.class);

        try{
            gameService.joinGame(joinData.playerColor(), joinData.gameID(), authToken);
            response.status(200);
            return "{}";
        } catch (DataAccessException e) {

            if(Objects.equals(e.getMessage(), "Game doesn't exist") || Objects.equals(e.getMessage(), "Bad request")){
                response.status(400);
                return "{ \"message\": \"Error: bad request\" }";
            } else if (Objects.equals(e.getMessage(), "Auth Token doesn't exist")) {
                response.status(401);
                return "{ \"message\": \"Error: unauthorized\" }";
            }
            response.status(403);
            return "{ \"message\": \"Error: already taken\" }";
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }

    }

    private Object listGames(Request request, Response response) {
        String authToken = request.headers("authorization");

        try{
            HashSet<GameData> games = gameService.listGames(authToken);
            HashSet<GameDataList> list = new HashSet<>();

            for(GameData game : games){
                var whiteUser = game.whiteUsername();
                var blackUser = game.blackUsername();

                GameDataList gameToAdd = new GameDataList(game.gameID(), whiteUser, blackUser, game.gameName());
                list.add(gameToAdd);
            }
            Gson gson = new Gson();

            response.status(200);
            return "{ \"games\": " + gson.toJsonTree(list) + "}";

        } catch (DataAccessException | ResponseException e) {
            response.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        }

    }

    private Object logoutUser(Request request, Response response) {
        String authToken = request.headers("authorization");
        try{
            userService.logoutUser(authToken);
            response.status(200);
            return "{}";
        } catch (DataAccessException | ResponseException e) {
            response.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        }
    }

    private Object loginUser(Request request, Response response) {

        LoginRequest loginRequest = new Gson().fromJson(request.body(), LoginRequest.class);

        try {
            var authToken = userService.loginUser(loginRequest.username(), loginRequest.password());
            response.status(200);
            return "{ \"username\": " + loginRequest.username() + ", \"authToken\": " + authToken + "}";
        } catch (DataAccessException e) {
            response.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }

    }

    private Object registerNewUser(Request request, Response response) throws DataAccessException {
        RegisterRequest registerRequest = new Gson().fromJson(request.body(), RegisterRequest.class);


        if(registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null){
            response.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        }

       try{
           var authToken = userService.createNewUser(registerRequest.username(), registerRequest.password(), registerRequest.email()).authToken();
           response.status(200);
           return "{ \"username\": " + registerRequest.username() + ", \"authToken\": " + authToken + "}";
       } catch (RuntimeException | ResponseException e) {
           response.status(403);
           return "{ \"message\": \"Error: already taken\" }";
       } catch (SQLException e) {
           throw new RuntimeException(e);
       }


    }

    private Object createGame(Request request, Response response) throws UnauthorizedException, BadRequestException, DataAccessException {
        if(!request.body().contains("\"gameName\":")){
            throw new BadRequestException("No gameName provided");
        }

        GameData gameData = new Gson().fromJson(request.body(), GameData.class);
        String authToken = request.headers("authorization");

       try{
           int gameID = gameService.createGame(gameData.gameName(), authToken);
           response.status(200);
           return "{ \"gameID\": " + gameID + "}";
       } catch (UnauthorizedException e) {
           response.status(401);
           return "{ \"message\": \"Error: unauthorized\" }";
       } catch (ResponseException e) {
           throw new RuntimeException(e);
       }


    }

    private Object clear(Request request, Response response) throws ResponseException {
        adminService.clear();

        response.status(200);
        return "{}";

    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
