package websocket.messages;

import com.google.gson.Gson;

public class ErrorMessage extends ServerMessage{

    private String errorMessage;
    public ErrorMessage(ServerMessageType serverMessageType, String errorMessage) {
        super(serverMessageType, errorMessage);
    }

    public String getErrorMessage(){
        return errorMessage;
    }

    public  void setErrorMessage(String message){
        this.errorMessage = message;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

}
