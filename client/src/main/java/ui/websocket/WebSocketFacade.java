package ui.websocket;

import chess.ChessBoard;
import chess.ChessMove;
import com.google.gson.Gson;
import exception.ResponseException;
import websocket.commands.UserGameCommand;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    Session session;
    NotificationHandler notificationHandler;
    int gameID;
    ChessBoard game;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        try{
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    notificationHandler.notify(message);
                }
            });
        } catch (DeploymentException | URISyntaxException | IOException e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        System.out.println("WebSocket connection established!");
    }

    public void joinGame(String authToken, Integer gameId) throws ResponseException {
        try{
            var action = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId, null);
            gameID = gameId;
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    public void leaveGame(String authToken) throws ResponseException {
        try{
            var action = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID, null);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
            this.session.close();
        } catch (IOException e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    public void resignGame(String authToken) throws ResponseException {
        try{
            var action = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID, null);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
            this.session.close();
        } catch (IOException e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    public void makeMove(String authToken, ChessMove move, Integer gameId) throws ResponseException {
        try{
            System.out.println(gameId);
            var action = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameId, move);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    public void observeGame(String authToken, Integer gameId) throws ResponseException {
        try{
            var action2 = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId, null);
            gameID = gameId;
            this.session.getBasicRemote().sendText(new Gson().toJson(action2));
        } catch (IOException e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    public ChessBoard getGame(){
        return game;
    }

}
