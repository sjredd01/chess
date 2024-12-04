package server.websocket;



import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import exception.ResponseException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;


import java.io.IOException;
import java.util.Objects;

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
    public void onMessage(Session session, String message) throws IOException, ResponseException, DataAccessException {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.getCommandType()){
            case CONNECT  -> enter(action.getUsername(), action.getGameID(), session);
            case LEAVE -> leave(action.getUsername(), action.getGameID(), session);
            case RESIGN -> resign(action.getUsername(), action.getGameID(), session);
        }
    }

    private void resign(String username, int gameID, Session session) throws ResponseException, DataAccessException {
        connections.remove(username);
        var message = String.format("resigned the game");
        gameDAO.getGame(gameID).game().endGame();
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
    }

    private void leave(String username, int gameID, Session session) throws IOException, ResponseException, DataAccessException {
        connections.remove(username);
        GameData gameInPlay = gameDAO.getGame(gameID);
        String gameName = gameInPlay.gameName();
        ChessGame game = gameInPlay.game();
        if(username.equals(gameInPlay.whiteUsername())){
            GameData newGame = new GameData(gameID, "", gameInPlay.blackUsername(), gameName, game);
            gameDAO.updateGame(newGame);
        } else if (username.equals(gameInPlay.blackUsername())) {
            GameData newGame = new GameData(gameID, gameInPlay.whiteUsername(), "", gameName, game);
            gameDAO.updateGame(newGame);
        }else{
            var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            connections.broadcast("", notification);
        }

        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        connections.broadcast("", notification);
    }

    private void enter(String username, int gameID, Session session) throws IOException {

        try{
            connections.add(username, session);
            var message = String.format(username + " has entered the game");
            ChessGame game = gameDAO.getGame(gameID).game();
            var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
            var notification1 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);

            connections.broadcastToOne(username, notification);
            connections.broadcast(username, notification1);

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
