package ui;

import chess.ChessGame;

import java.util.Scanner;

public class Repl {
    private final ChessClient client;
    public static PrintBoard printBoard;

    public Repl(String serverURL) {
        this.client = new ChessClient(serverURL);
    }

    public void run(){
        //System.out.println(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            //printPrompt();
            System.out.println("\n" + client.help());
            String line = scanner.nextLine();


            try {
                if(line.contains("observe") && line.matches(".*\\d+.*")){
                    printBoard = new PrintBoard(new ChessGame());
                    printBoard.printBoard(ChessGame.TeamColor.WHITE, null);
                    System.out.println("observing game\n");
                }else {


                    result = client.eval(line);
                    if (line.contains("join") && line.matches(".*\\d+.*")) {
                        printBoard = new PrintBoard(new ChessGame());
                        printBoard.printBoard(ChessGame.TeamColor.BLACK, null);
                        printBoard.printBoard(ChessGame.TeamColor.WHITE, null);
                    }
                    System.out.print(result + "\n");
                }

            } catch (Throwable e) {
                var msg = e.getMessage();
                System.out.print(msg);
            }
        }
        System.out.println();
    }
}
