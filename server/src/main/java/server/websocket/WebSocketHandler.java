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

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private UserDAO userDAO;

    public WebSocketHandler(GameDAO gameDAO1, AuthDAO authDAO1, UserDAO userDAO1){
        gameDAO = gameDAO1;
        authDAO = authDAO1;
        userDAO = userDAO1;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, ResponseException, DataAccessException, InvalidMoveException {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.getCommandType()){
            case CONNECT  -> enter(action.getAuthToken(), action.getGameID(), session);
            case LEAVE -> leave(action.getAuthToken(), action.getGameID(), session);
            case RESIGN -> resign(action.getAuthToken(), action.getGameID(), session);
            case MAKE_MOVE -> makeMove(action.getAuthToken(), action.getGameID(), action.getMove(), session);
        }
    }

    private void makeMove(String authToken, Integer gameID, ChessMove move, Session session) throws ResponseException, DataAccessException, IOException, InvalidMoveException {

        GameData gameInPlay = gameDAO.getGame(gameID);
        ChessGame game = gameInPlay.game();
        ChessGame.TeamColor userTeam = ChessGame.TeamColor.WHITE;
        ChessGame.TeamColor enemyTeam = ChessGame.TeamColor.BLACK;
        Boolean validMove = false;

        if(authorized(authToken)){
            String username = authDAO.getAuth(authToken).username();

            if(username.equals(gameInPlay.blackUsername()) || username.equals(gameInPlay.whiteUsername())) {
                if (username.equals(gameInPlay.blackUsername())) {
                    userTeam = ChessGame.TeamColor.BLACK;
                    enemyTeam = ChessGame.TeamColor.WHITE;
                }
                if(gameInPlay.game().getBoard().getPiece(move.getStartPosition()).getTeamColor() == userTeam) {
                    if (!game.checkGameStatus()) {
                        for (ChessMove moves : game.validMoves(move.getStartPosition())) {
                            if (moves.equals(move)) {
                                try {
                                    game.makeMove(move);
                                    game.setTeamTurn(enemyTeam);
                                    GameData newGame = new GameData(gameID, gameInPlay.whiteUsername(), gameInPlay.blackUsername(), gameInPlay.gameName(), game);
                                    gameDAO.updateGame(newGame);
                                    GameData updatedGame = gameDAO.getGame(gameID);
                                    var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, updatedGame);
                                    connections.broadcast("", notification);
                                    var message = String.format("message: " + userTeam + " team made move " + move);
                                    var notificationForMove = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
                                    connections.broadcast(authToken, notificationForMove);

                                    if (game.isInCheck(enemyTeam)) {
                                        var message1 = String.format("Check");
                                        var notification1 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message1);
                                        connections.broadcast("", notification1);
                                    }else if (game.isInCheckmate(enemyTeam)) {
                                        var message2 = String.format("Checkmate");
                                        var notification2 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message2);
                                        game.endGame();
                                        connections.broadcast("", notification2);
                                    }

                                    if (game.isInStalemate(enemyTeam)) {
                                        var message3 = String.format("Checkmate");
                                        var notification3 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message3);
                                        game.endGame();
                                        connections.broadcast("", notification3);
                                    }
                                } catch (InvalidMoveException e) {
                                    var errorMessage = String.format("ERROR: Wrong turn");
                                    var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
                                    connections.broadcastToOne(authToken, notification);
                                }
                            }

                            validMove = true;
                        }

                        if (!validMove) {
                            var errorMessage = String.format("ERROR: Invalid move");
                            var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
                            connections.broadcastToOne(authToken, notification);
                        }

                    } else {
                        var errorMessage = String.format("ERROR: Can not play on a game that finished");
                        var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
                        connections.broadcastToOne(authToken, notification);
                    }
                }else{
                    var errorMessage = String.format("ERROR: Can not move enemy piece");
                    var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
                    connections.broadcastToOne(authToken, notification);
                }
            }else{
                var errorMessage = String.format("ERROR: Observer can not make a move");
                var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
                connections.broadcastToOne(authToken, notification);
            }
        }else{
            connections.add(authToken, session);
            var errorMessage = String.format("ERROR: Unauthorized");
            var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
            connections.broadcastToOne(authToken, notification);
        }

    }

    private void resign(String authToken, int gameID, Session session) throws ResponseException, DataAccessException, IOException {
        String username = authDAO.getAuth(authToken).username();
        GameData gameInPlay = gameDAO.getGame(gameID);
        if(username.equals(gameInPlay.blackUsername()) || username.equals(gameInPlay.whiteUsername())) {
            ChessGame updatedGame = gameInPlay.game();
            if (!updatedGame.checkGameStatus()) {
                var message = String.format("resigned the game");
                updatedGame.endGame();
                GameData newGame = new GameData(gameID, gameInPlay.whiteUsername(), gameInPlay.blackUsername(), gameInPlay.gameName(), updatedGame);
                gameDAO.updateGame(newGame);
                var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
                connections.broadcast("", notification);
                connections.remove(authToken);
            } else {
                var errorMessage = String.format("ERROR: Can not play on a game that finished");
                var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
                connections.broadcastToOne(authToken, notification);
            }
        }else{
            var errorMessage = String.format("ERROR: Observer can not resign");
            var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
            connections.broadcastToOne(authToken, notification);
        }
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

    private void enter(String authToken, int gameID, Session session) throws IOException, ResponseException, DataAccessException {

        if(authorized(authToken)) {
            try {
                connections.add(authToken, session);
                var message = String.format("message: " + authToken + " has entered the game");
                GameData game = gameDAO.getGame(gameID);
                var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
                var notification1 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);

                try {
                    connections.broadcastToOne(authToken, notification);
                    connections.broadcast(authToken, notification1);
                } catch (IOException e) {
                    System.err.println("Error in broadcasting message to user");
                }

            } catch (RuntimeException e) {
                var errorMessage = String.format("ERROR: Game does not exist!!!!");
                var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
                connections.broadcast("", notification);
                throw new RuntimeException(e);
            } catch (ResponseException | DataAccessException e) {
                var errorMessage = String.format("ERROR: Game does not exist");
                var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
                connections.broadcastToOne(authToken, notification);
            }
        }else{
            connections.add(authToken, session);
            var errorMessage = String.format("ERROR: Unauthorized");
            var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
            connections.broadcastToOne(authToken, notification);
        }

    }

    private boolean authorized(String authToken) throws IOException {
        boolean authGood = true;

        try{
            authDAO.getAuth(authToken);
        } catch (DataAccessException | ResponseException e) {
            authGood = false;
            return authGood;
        }

        return authGood;
    }

}
