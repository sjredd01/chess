package server.websocket;



import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import exception.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;


import javax.swing.*;

import java.io.IOException;

import static websocket.commands.UserGameCommand.CommandType.CONNECT;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserDAO userDAO;

    public WebSocketHandler(GameDAO gameDAO, AuthDAO authDAO, UserDAO userDAO){
        gameDAO = this.gameDAO;
        authDAO = this.authDAO;
        userDAO = this.userDAO;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.getCommandType()){
            case CONNECT  -> enter(action.getAuthToken(), action.getGameID(), session);
            case LEAVE -> leave(action.getAuthToken(), session);
            case RESIGN -> resign(action.getAuthToken(), session);
        }
    }

    private void resign(String authToken, Session session) {
        connections.remove("");
        var message = String.format("resigned the game");
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
    }

    private void leave(String authToken, Session session) throws IOException {
        connections.remove(authToken);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        connections.broadcast("", notification);
    }

    private void enter(String authToken, int gameID,  Session session) throws IOException {

        try{
            connections.add(authToken, session);
            var message = String.format(authToken + " has entered the game");
            ChessGame game = gameDAO.getGame(gameID).game();
            var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
            var notification1 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);

            connections.broadcast("", notification);
            connections.broadcast(authToken, notification1);

        } catch (RuntimeException e) {
            var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            connections.broadcast("", notification);
            throw new RuntimeException(e);
        } catch (ResponseException | DataAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public void joinGame(String color, String username) throws ResponseException {
        try{
            var message = String.format(username + " has joined the game as " + color);
            var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            connections.broadcast("", notification);
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }

    }
}
