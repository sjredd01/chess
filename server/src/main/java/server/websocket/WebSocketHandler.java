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

    private void makeMove(String authToken, Integer gameID, ChessMove move, Session session) throws ResponseException,
            DataAccessException, IOException, InvalidMoveException {

        if (!authorized(authToken)) {
            handleUnauthorized(authToken, session);
            return;
        }

        String username = authDAO.getAuth(authToken).username();
        GameData gameInPlay = gameDAO.getGame(gameID);
        ChessGame game = gameInPlay.game();

        if (!isUserPartOfGame(username, gameInPlay)) {
            sendError(authToken, "ERROR: Observer can not make a move");
            return;
        }

        ChessGame.TeamColor userTeam = getUserTeam(username, gameInPlay);
        ChessGame.TeamColor enemyTeam = getEnemyTeam(userTeam);

        if (!isUserPiece(userTeam, move, game)) {
            sendError(authToken, "ERROR: Can not move enemy piece");
            return;
        }

        if (game.checkGameStatus()) {
            sendError(authToken, "ERROR: Can not play on a game that finished");
            return;
        }

        if (!isValidMove(move, game)) {
            sendError(authToken, "ERROR: Invalid move");
            return;
        }

        try {
            game.makeMove(move);
            game.setTeamTurn(enemyTeam);
            updateGameState(gameID, gameInPlay, game);
            broadcastMoveNotifications(authToken, userTeam, move, game, enemyTeam, gameID);
        } catch (InvalidMoveException e) {
            sendError(authToken, "ERROR: Wrong turn");
        }
    }

    private void handleUnauthorized(String authToken, Session session) throws IOException {
        connections.add(authToken, session);
        sendError(authToken, "ERROR: Unauthorized");
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

    private void broadcastMoveNotifications(String authToken, ChessGame.TeamColor userTeam,
                                            ChessMove move, ChessGame game, ChessGame.TeamColor enemyTeam, int gameID)
            throws IOException, ResponseException, DataAccessException {
        GameData updatedGame = gameDAO.getGame(gameID);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, updatedGame);
        connections.broadcast("", notification);

        String message = String.format("message: " + userTeam + " team made move " + move);
        var notificationForMove = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(authToken, notificationForMove);

        sendCheckStatusNotifications(game, enemyTeam);
    }

    private void sendCheckStatusNotifications(ChessGame game, ChessGame.TeamColor enemyTeam) throws IOException {
        if (game.isInCheck(enemyTeam)) {
            sendCheckNotification("Check");
        } else if (game.isInCheckmate(enemyTeam)) {
            sendCheckNotification("Checkmate");
            game.endGame();
        } else if (game.isInStalemate(enemyTeam)) {
            sendCheckNotification("Stalemate");
            game.endGame();
        }
    }

    private void sendCheckNotification(String message) throws IOException {
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast("", notification);
    }

    private void sendError(String authToken, String errorMessage) throws IOException {
        var notification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, errorMessage);
        connections.broadcastToOne(authToken, notification);
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
        connections.broadcast(authToken, notification);
        connections.remove(authToken);
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
