package ui;


import ui.websocket.NotificationHandler;
import websocket.messages.ServerMessage;
import java.util.Scanner;

public class Repl implements NotificationHandler {
    private final ChessClient client;
    public Repl(String serverURL) {
        this.client = new ChessClient(serverURL, this);
    }

    public void run(){

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            System.out.println("\n" + client.help());
            String line = scanner.nextLine();


            try {
                    result = client.eval(line);
                    System.out.print(result + "\n");
            } catch (Throwable e) {
                var msg = e.getMessage();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    @Override
    public void notify(ServerMessage notification) {

    }
}
