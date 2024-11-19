package ui.websocket;

import websocket.commands.UserGameCommand;

import javax.management.Notification;

public interface NotificationHandler {
    void notify(Notification notification);
}
