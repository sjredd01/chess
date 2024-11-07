package ui;

import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import server.ServerFacade;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private final String serverURL;
    private final String authToken = null;
    private State state = State.LOGGEDOUT;
    private ServerFacade sf;

    public ChessClient(String serverURL){
        server = new ServerFacade(serverURL);
        this.serverURL = serverURL;
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
                case "logout" -> logOut();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            server.registerUser(newUser);

            return newUser.username() + " is now registered\n";
        }

        throw new ResponseException(400, "Expected <USERNAME> <PASSWORD> <EMAIL>");
    }

    private String logIn(String... param) throws ResponseException {
        if(param.length >= 1){
            var username = param[0];
            var password = param[1];
            UserData user = new UserData(username, password, null);
            server.logIn(user);
            state = State.LOGGEDIN;

            return user.username() + " is now logged in\n";
        }

        throw new ResponseException(400, "Expected <USERNAME> <PASSWORD>");
    }

    private String createGame(String ... param) {
        var gameName = param[0];
        GameData newGame = new GameData(0, "n", "n", gameName, null);

        server.createGame(newGame);

        return gameName + " is now created\n";
    }
}
