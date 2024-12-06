package server.websocket;

import chess.ChessBoard;
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

    private void makeMove(String authToken, Integer gameID, ChessMove move, Session session) throws ResponseException,
            DataAccessException, IOException, InvalidMoveException {
        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            handleUnauthorized(gameID, authToken, session);
        }
        String username = authDAO.getAuth(authToken).username();
        if (username == null) {
            handleUnauthorized(gameID, authToken, session);
        }


        GameData gameInPlay = gameDAO.getGame(gameID);
        ChessGame game = gameInPlay.game();


        ChessGame.TeamColor userTeam = getUserTeam(username, gameInPlay);
        ChessGame.TeamColor enemyTeam = getEnemyTeam(userTeam);

        if (!isUserPartOfGame(username, gameInPlay)) {
            sendError(username, "ERROR: Observer can not make a move", gameID);
        } else if (!isUserPiece(userTeam, move, game)) {
            sendError(username, "ERROR: Can not move enemy piece", gameID);
        } else if (game.checkGameStatus()) {
            sendError(username, "ERROR: Can not play on a game that finished", gameID);
        } else if (!isValidMove(move, game)) {
            sendError(username, "ERROR: Invalid move", gameID);
        } else {

        try {
            game.makeMove(move);
            game.setTeamTurn(enemyTeam);
            updateGameState(gameID, gameInPlay, game);
            broadcastMoveNotifications(username, userTeam, move, game, enemyTeam, gameID);
        } catch (InvalidMoveException e) {
            sendError(username, "ERROR: Wrong turn", gameID);
        }
    }
    }

    private void handleUnauthorized(int gameID, String authToken, Session session) throws IOException {
        connections.add(gameID, authToken, session);
        sendError(authToken, "ERROR: Unauthorized", gameID);
        connections.remove(authToken);
    }

    private boolean isUserPartOfGame(String username, GameData gameInPlay) {
        return username.equals(gameInPlay.blackUsername()) || username.equals(gameInPlay.whiteUsername());
    }

    private ChessGame.TeamColor getUserTeam(String username, GameData gameInPlay) {
        if (username.equals(gameInPlay.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }
        return ChessGame.TeamColor.WHITE;
    }

    private ChessGame.TeamColor getEnemyTeam(ChessGame.TeamColor userTeam) {
        return userTeam == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
    }

    private boolean isUserPiece(ChessGame.TeamColor userTeam, ChessMove move, ChessGame game) {
        return game.getBoard().getPiece(move.getStartPosition()).getTeamColor() == userTeam;
    }

    private boolean isValidMove(ChessMove move, ChessGame game) {
        for (ChessMove validMove : game.validMoves(move.getStartPosition())) {
            if (validMove.equals(move)) {
                return true;
            }
        }
        return false;
    }

    private void updateGameState(Integer gameID, GameData gameInPlay, ChessGame game) throws DataAccessException, ResponseException {
        GameData newGame = new GameData(gameID, gameInPlay.whiteUsername(), gameInPlay.blackUsername(), gameInPlay.gameName(), game);
        gameDAO.updateGame(newGame);
    }

    private void broadcastMoveNotifications(String username, ChessGame.TeamColor userTeam,
                                            ChessMove move, ChessGame game, ChessGame.TeamColor enemyTeam, int gameID)
            throws IOException, ResponseException, DataAccessException {

        var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game.getBoard());
        connections.broadcast(gameID,"", notification);

        String message = String.format("message: " + userTeam + " team moved " + move.getStartPosition().toString() + " to " + move.getEndPosition().toString());
        var notificationForMove = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(gameID, username, notificationForMove);

        sendCheckStatusNotifications(game, enemyTeam, gameID);
    }

    private void sendCheckStatusNotifications(ChessGame game, ChessGame.TeamColor enemyTeam, int gameID) throws IOException {
        if (game.isInCheckmate(enemyTeam)) {
            sendCheckNotification("Checkmate", gameID);
            game.endGame();
        } else if (game.isInStalemate(enemyTeam)) {
            sendCheckNotification("Stalemate", gameID);
            game.endGame();
        }else if (game.isInCheck(enemyTeam)) {
            sendCheckNotification("Check", gameID);
        }
    }

    private void sendCheckNotification(String message, int gameID) throws IOException {
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast( gameID,"", notification);
    }

    private void sendError(String authToken, String errorMessage, int gameID) throws IOException {
        var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
        connections.broadcastToOne(gameID, authToken, notification);
    }


    private void resign(String authToken, int gameID, Session session) throws ResponseException, DataAccessException, IOException {
        String username = authDAO.getAuth(authToken).username();
        GameData gameInPlay = gameDAO.getGame(gameID);
        if(username.equals(gameInPlay.blackUsername()) || username.equals(gameInPlay.whiteUsername())) {
            ChessGame updatedGame = gameInPlay.game();
            if (!updatedGame.checkGameStatus()) {
                var message = String.format(username + "resigned the game");
                updatedGame.endGame();
                GameData newGame = new GameData(gameID, gameInPlay.whiteUsername(), gameInPlay.blackUsername(), gameInPlay.gameName(), updatedGame);
                gameDAO.updateGame(newGame);
                var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
                connections.broadcast(gameID,"", notification);
                connections.remove(username);
            } else {
                var errorMessage = String.format("ERROR: Can not play on a game that finished");
                var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
                connections.broadcastToOne(gameID, username, notification);
            }
        }else{
            var errorMessage = String.format("ERROR: Observer can not resign");
            var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
            connections.broadcastToOne(gameID, username, notification);
        }
    }

    private void leave(String authToken, int gameID, Session session) throws IOException, ResponseException, DataAccessException {
        String username = authDAO.getAuth(authToken).username();

        GameData gameInPlay = gameDAO.getGame(gameID);
        String gameName = gameInPlay.gameName();
        ChessGame game = gameInPlay.game();
        if(username.equals(gameInPlay.whiteUsername())){
            GameData newGame = new GameData(gameID, null, gameInPlay.blackUsername(), gameName, game);
            gameDAO.updateGame(newGame);
        } else if (username.equals(gameInPlay.blackUsername())) {
            GameData newGame = new GameData(gameID, gameInPlay.whiteUsername(), null, gameName, game);
            gameDAO.updateGame(newGame);
        }

        var message = String.format("message: " + username + " has left the game");
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(gameID, username, notification);
        connections.remove(username);
    }

    private void enter(String authToken, int gameID, Session session) throws IOException, ResponseException, DataAccessException {

        try {
            String username = authDAO.getAuth(authToken).username();
            try {
                connections.add(gameID, username, session);
                var message = String.format("message: " + username + " has entered the game");
                GameData gameData = gameDAO.getGame(gameID);
                ChessBoard game = gameData.game().getBoard();
                System.out.print(game.toString());
                var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
                var notification1 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);

                try {
                    connections.broadcastToOne(gameID, username, notification);
                    connections.broadcast(gameID, username, notification1);
                } catch (IOException e) {
                    System.err.println("Error in broadcasting message to user");
                }

            } catch (RuntimeException e) {
                var errorMessage = String.format("ERROR: Game does not exist!!!!");
                var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
                connections.broadcastToOne(gameID, username, notification);
                throw new RuntimeException(e);
            } catch (ResponseException | DataAccessException e) {
                var errorMessage = String.format("ERROR: Game does not exist");
                var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
                connections.broadcastToOne(gameID, username, notification);
            }
        }catch (DataAccessException e){
            connections.add(gameID, authToken , session);
            var errorMessage = String.format("ERROR: Unauthorized");
            var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
            connections.broadcastToOne(gameID, authToken, notification);
            connections.remove(authToken);
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
