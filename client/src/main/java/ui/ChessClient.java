package ui;

import server.ServerFacade;

public class ChessClient {
    private final ServerFacade server;

    public ChessClient(String serverURL){
        server = new ServerFacade(serverURL);
    }
}
