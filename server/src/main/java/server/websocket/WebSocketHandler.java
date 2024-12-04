package server.websocket;



import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
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
    public void onMessage(Session session, String message) throws IOException, ResponseException, DataAccessException, InvalidMoveException {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.getCommandType()){
            case CONNECT  -> enter(action.getUsername(), action.getGameID(), session);
            case LEAVE -> leave(action.getUsername(), action.getGameID(), session);
            case RESIGN -> resign(action.getUsername(), action.getGameID(), session);
            case MAKE_MOVE -> makeMove(action.getUsername(), action.getGameID(), action.getMove(), session);
        }
    }

    private void makeMove(String username, Integer gameID, ChessMove move, Session session) throws ResponseException, DataAccessException, IOException, InvalidMoveException {
        GameData gameInPlay = gameDAO.getGame(gameID);
        ChessGame game = gameInPlay.game();
        ChessGame.TeamColor userTeam = ChessGame.TeamColor.WHITE;

        if(username.equals(gameInPlay.blackUsername())){
            userTeam = ChessGame.TeamColor.BLACK;
        }

        if(!game.checkGameStatus()){
            for(ChessMove moves : game.validMoves(move.getStartPosition())){
                if(moves.equals(move)){
                    game.makeMove(move);
                    GameData newGame = new GameData(gameID, gameInPlay.whiteUsername(), gameInPlay.blackUsername(), gameInPlay.gameName(), game);
                    gameDAO.updateGame(newGame);
                    ChessGame updatedGame = gameDAO.getGame(gameID).game();
                    var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, updatedGame);
                    connections.broadcast("", notification);
                    var notificationForMove = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                    connections.broadcast(username, notificationForMove);

                    if(game.isInCheck(ChessGame.TeamColor.BLACK)){
                        var notification1 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                        connections.broadcast("", notification1);
                    }

                    if(game.isInCheckmate(ChessGame.TeamColor.BLACK)){
                        var notification2 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                        game.endGame();
                        connections.broadcast("", notification2);
                    }

                    if(game.isInStalemate(ChessGame.TeamColor.BLACK)){
                        var notification3 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                        game.endGame();
                        connections.broadcast("", notification3);
                    }
                }
            }
            
            var invalidMoveNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            connections.broadcastToOne(username, invalidMoveNotification);



        }else{
            var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            connections.broadcastToOne(username, notification);
        }

    }

    private void resign(String username, int gameID, Session session) throws ResponseException, DataAccessException, IOException {
        connections.remove(username);
        var message = String.format("resigned the game");
        gameDAO.getGame(gameID).game().endGame();
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        connections.broadcast("", notification);
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
            connections.broadcast(username, notification);
        }

        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        connections.broadcast(username, notification);
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

}
