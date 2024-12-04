package ui;

import chess.ChessGame;
import ui.websocket.NotificationHandler;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.management.Notification;
import java.util.Scanner;

public class Repl implements NotificationHandler {
    private final ChessClient client;
    public static PrintBoard printBoard;

    public Repl(String serverURL) {
        this.client = new ChessClient(serverURL, this);
    }

    public void run(){
        //System.out.println(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            System.out.println("\n" + client.help());
            String line = scanner.nextLine();


            try {
                if(line.contains("observe") && line.matches(".*\\d+.*")){
                    printBoard = new PrintBoard(new ChessGame());
                    printBoard.printBoard(ChessGame.TeamColor.WHITE, null);
                    System.out.println("observing game\n");
                }else {


                    result = client.eval(line);
//                    if (line.contains("join") && line.matches(".*\\d+.*")) {
//                        printBoard = new PrintBoard(new ChessGame());
//                        printBoard.printBoard(ChessGame.TeamColor.BLACK, null);
//                        printBoard.printBoard(ChessGame.TeamColor.WHITE, null);
//                    }
                    System.out.print(result + "\n");
                }

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
