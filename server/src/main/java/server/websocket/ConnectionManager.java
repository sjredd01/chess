package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String visitor, Session session){
        var connection = new Connection(visitor, session);
        connections.put(visitor, connection);
    }

    public void remove(String visitor){
        connections.remove(visitor);
    }

    public void broadcast(String excludeVisitor, ServerMessage message) throws IOException {
        var removedList = new ArrayList<Connection>();

        for(var con : connections.values()){
            if(con.session.isOpen()){
                if(!con.visitor.equals(excludeVisitor)){
                    con.send(message.toString());
                }
            }else{
                removedList.add(con);
            }
        }

        for(var con : removedList){
            connections.remove(con.visitor);
        }
    }

    public void broadcastToOne(String visitor, ServerMessage message) throws IOException {
        var removedList = new ArrayList<Connection>();

        for(var con : connections.values()){
            if(con.session.isOpen()){
                if(con.visitor.equals(visitor)){
                    con.send(message.toString());
                }
            }else{
                removedList.add(con);
            }
        }

        for(var con : removedList){
            connections.remove(con.visitor);
        }
    }
}
