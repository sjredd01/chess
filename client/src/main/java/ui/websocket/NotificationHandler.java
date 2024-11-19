package ui.websocket;

import websocket.messages.ServerMessage;
import websocket.commands.UserGameCommand;

public interface NotificationHandler {
    void notify(ServerMessage serverMessage);

    void command(UserGameCommand userGameCommand);
}
