package ui;

public class Repl {
    private final ChessClient client;

    public Repl(String serverURL) {
        this.client = new ChessClient(serverURL);
    }
}
