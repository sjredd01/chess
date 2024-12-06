package ui;


import chess.ChessBoard;
import chess.ChessGame;
import com.google.gson.Gson;
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


    public void notify(String notification) {
        ServerMessage serverMessage = new Gson().fromJson(notification, ServerMessage.class);
        switch(serverMessage.getServerMessageType()){
            case LOAD_GAME:
                loadGame(serverMessage);
                break;
            case ERROR:
                printError(serverMessage);
                break;
            case NOTIFICATION:
                printNotification(serverMessage);
                break;
        }
    }

    private void loadGame(ServerMessage message){
        ChessBoard board = message.getGame();
        PrintBoard printer = new PrintBoard(board);
        ChessGame.TeamColor team = client.getUserColor();
        printer.printBoard(team, null);
    }
    private void printError(ServerMessage message) {
        String error = message.getErrorMessage();
        System.out.println(error);
    }
    private void printNotification(ServerMessage message){
        String message1 = message.getMessage();
        System.out.println(message1);
    }
}
