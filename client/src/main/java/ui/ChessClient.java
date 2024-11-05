package ui;

import exception.ResponseException;
import model.UserData;
import server.ServerFacade;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private final String serverURL;
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
                case "login" -> logIn(param);
                default -> help();
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String registerUser(String... param) throws ResponseException {
        if(param.length >= 3){
            var username = param[0];
            var password = param[1];
            var email = param[2];
            var newUser = new UserData(username, password, email);
            newUser = server.registerUser(newUser);

            return newUser.username() + " is now registered";
        }

        throw new ResponseException(400, "Expected <USERNAME> <PASSWORD> <EMAIL>");
    }

    private String logIn(String[] param) throws ResponseException {
        if(param.length >= 1){
            state = State.LOGGEDIN;
            sf = new ServerFacade(serverURL);
            return "you are signed in ";
        }

        throw new ResponseException(400, "Expected other");
    }
}
