package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, ArrayList<Connection>> connections = new ConcurrentHashMap<>();

    public void add(Integer gameID, String visitor, Session session){
        var connection = new Connection(visitor, session);

        if(connections.contains(gameID)){
            var inGame = connections.get(gameID);
            inGame.add(connection);
        }else{
            ArrayList<Connection> gamePlayer = new ArrayList<>();
            gamePlayer.add(connection);
            connections.put(gameID, gamePlayer);
        }

    }

    public void remove(String visitor){
        for(var gameConnections : connections.values()){
            gameConnections.removeIf(connection -> connection.visitor.equals(visitor));
        }
    }

    public void broadcast(Integer gameid, String excludeVisitor, ServerMessage message) throws IOException {
        var inGame = connections.get(gameid);

        for(var conn : inGame){
            if(conn.session.isOpen()){
                if(!conn.visitor.equals(excludeVisitor)){
                    conn.send(message.toString());
                }
            }
        }

    }

    public void broadcastToOne(Integer gameid, String visitor, ServerMessage message) throws IOException {
        var inGame = connections.get(gameid);

        for(var conn : inGame){
            if(conn.session.isOpen()){
                if(conn.visitor.equals(visitor)){
                    conn.send(message.toString());
                }
            }
        }

    }
}
