package ui;

import server.ServerFacade;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;

    public ChessClient(String serverURL){
        server = new ServerFacade(serverURL);
    }


    public String help(){
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
                default -> help();
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
