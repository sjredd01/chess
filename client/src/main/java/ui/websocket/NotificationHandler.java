package ui.websocket;

import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.management.Notification;

public interface NotificationHandler {
    void notify(ServerMessage notification);
}
