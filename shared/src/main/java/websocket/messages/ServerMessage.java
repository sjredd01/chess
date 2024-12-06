package websocket.messages;

import chess.ChessBoard;
import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;

    private ChessBoard game;
    private String message;
    private String errorMessage;


    public ServerMessage(ServerMessageType serverMessageType, String message) {
        this.serverMessageType = serverMessageType;
        this.message = message;

        if(serverMessageType == ServerMessageType.ERROR){
            this.message = null;
            this.errorMessage = message;
        }
//        this.errorMessage = message;
    }


    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type, ChessBoard game) {
        this.serverMessageType = type;
        this.game = game;
    }

    public ChessBoard getGame() {
        return this.game;
    }

    public void setGame(ChessBoard game) {
        this.game = game;
    }

    public String getMessage(){
        return message;
    }

    public  void setMessage(String message){
        this.message = message;
    }

    public String getErrorMessage(){
        return errorMessage;
    }

    public  void setErrorMessage(String message){
        this.errorMessage = message;
    }


    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public String toString() {
        return new Gson().toJson(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
