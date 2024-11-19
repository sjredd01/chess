package server.websocket;
import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;

public class Connection {
    public String visitor;
    public Session session;

    public Connection(String visitor, Session session){
        this.visitor = visitor;
        this.session = session;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }
}
