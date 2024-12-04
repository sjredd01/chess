package ui;

import exception.ResponseException;
import model.GameData;
import model.GameDataList;
import model.UserData;
import server.ServerFacade;
import ui.websocket.NotificationHandler;
import ui.websocket.WebSocketFacade;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

public class ChessClient {
    private final ServerFacade server;
    private String username1 = null;
    private final String serverURL;
    private State state = State.LOGGEDOUT;
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;
    private HashSet<GameDataList> games;

    public ChessClient(String serverURL, NotificationHandler notificationHandler){
        server = new ServerFacade(serverURL);
        this.serverURL = serverURL;
        this.notificationHandler = notificationHandler;
    }


    public String help(){
       if(state == State.LOGGEDIN){
           return """
                create <NAME>
                list
                join <ID> [WHITE|BLACK]
                observe <ID>
                logout
                quit
                help
                """;
       }
        return """
                register <USERNAME> <PASSWORD> <EMAIL>
                login <USERNAME> <PASSWORD>
                quit
                help
                """;
    }

    public String eval(String line) {
        try{
            var token = line.toLowerCase().split(" ");
            var cmd = (token.length > 0) ? token[0] : "help";
            var param = Arrays.copyOfRange(token, 1, token.length);
            return switch (cmd){
                case "register" -> registerUser(param);
                case "create" -> createGame(param);
                case "login" -> logIn(param);
                case "join" -> joinGame(param);
                case "logout" -> logOut();
                case "list" -> listGames();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (Exception e) {
            return (e).getMessage();
        }
    }

    private String joinGame(String[] param) throws ResponseException, URISyntaxException {
        if(param.length >= 2){
            var gameID = Integer.parseInt(param[0]);
            var teamColor = param[1].toUpperCase();
            int gameToJoin = 0;
            int count = 1;
            for(var game : games){
                if(count == gameID){
                    gameToJoin = game.gameID();
                }
                count ++;
            }

            if(gameToJoin == 0){
                return "Game does not exist";
            }

            server.joinGame(gameToJoin, teamColor);
            ws = new WebSocketFacade(serverURL, notificationHandler);
            ws.joinGame(username1, gameID);

            return "joined game " + gameID;
        }

        throw new ResponseException(400, "Expected <ID> [WHITE|BLACK]");
    }

    private String listGames() {
        games = server.listGames();
        var result = new StringBuilder();
        int count = 1;

        for(var game : games){
            result.append(count).append(") Game Name: ").append(game.gameName()).append("\n");
            if(game.whiteUsername() != null){
                result.append("White Username: ").append(game.whiteUsername()).append("\n");
            }
            if(game.blackUsername() != null){
                result.append("Black Username: ").append(game.blackUsername()).append("\n");
            }
            result.append("\n");

            count ++;

        }

       return result.toString();
    }

    private String logOut() {

        server.logOut();
        state = State.LOGGEDOUT;

        return "Logged out\n";
    }

    private String registerUser(String... param) throws ResponseException {
        if(param.length >= 3){
            var username = param[0];
            var password = param[1];
            var email = param[2];
            var newUser = new UserData(username, password, email);
            username1 = username;
            server.registerUser(newUser);
            state = State.LOGGEDIN;

            return newUser.username() + " is now registered\n";
        }

        throw new ResponseException(400, "Expected <USERNAME> <PASSWORD> <EMAIL>");
    }

    private String logIn(String... param) throws ResponseException {
        if(param.length >= 2){
            var username = param[0];
            var password = param[1];
            UserData user = new UserData(username, password, null);
            username1 = username;
            server.logIn(user);
            state = State.LOGGEDIN;

            return user.username() + " is now logged in\n";
        }

        throw new ResponseException(400, "Expected <USERNAME> <PASSWORD>");
    }

    private String createGame(String ... param) throws ResponseException {
        if(param.length >= 1){
            var gameName = param[0];
            GameData newGame = new GameData(0, "n", "n", gameName, null);

            server.createGame(newGame);

            return " Game " + gameName + " is now created\n";
        }

        throw new ResponseException(400, "Expected <NAME>");

    }
}
