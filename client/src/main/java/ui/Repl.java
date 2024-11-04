package ui;

import java.util.Scanner;

public class Repl {
    private final ChessClient client;

    public Repl(String serverURL) {
        this.client = new ChessClient(serverURL);
    }

    public void run(){
        System.out.println(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            //printPrompt();
            String line = scanner.nextLine();

            try {

               result = client.eval(line);
               System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }
}
