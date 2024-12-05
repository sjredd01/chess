package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import exception.ResponseException;
import model.GameData;
import model.GameDataList;
import model.UserData;
import server.ServerFacade;
import ui.websocket.NotificationHandler;
import ui.websocket.WebSocketFacade;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class ChessClient {
    private final ServerFacade server;
    private String username1 = null;
    private final String serverURL;
    private State state = State.LOGGEDOUT;
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;
    private HashSet<GameDataList> games;
    public static PrintBoard printBoard;
    private ChessGame.TeamColor userColor = ChessGame.TeamColor.WHITE;
    private GameData game;

    public ChessClient(String serverURL, NotificationHandler notificationHandler){
        server = new ServerFacade(serverURL);
        this.serverURL = serverURL;
        this.notificationHandler = notificationHandler;
    }


    public String help(){
       if(state == State.LOGGEDIN){
           return """
                create <NAME>
                list
                join <ID> [WHITE|BLACK]
                observe <ID>
                logout
                quit
                help
                """;
       }

       if(state == State.GAMEPLAY){
           return """
                redraw
                leave
                make-move <STARTPOSITION> <ENDPOSITION>
                resign
                highlight <POSITION>
                help
                """;
       }
        return """
                register <USERNAME> <PASSWORD> <EMAIL>
                login <USERNAME> <PASSWORD>
                quit
                help
                """;
    }

    public String eval(String line) {
        try{
            var token = line.toLowerCase().split(" ");
            var cmd = (token.length > 0) ? token[0] : "help";
            var param = Arrays.copyOfRange(token, 1, token.length);
            return switch (cmd){
                case "register" -> registerUser(param);
                case "create" -> createGame(param);
                case "login" -> logIn(param);
                case "join" -> joinGame(param);
                case "logout" -> logOut();
                case "list" -> listGames();
                case "redraw" -> redrawBoard();
                case "highlight" -> highlightMoves(param);
                case "observe" -> observeGame(param);
                case "leave" -> leaveGame();
                case "make-move" -> makeMove(param);
                case "resign" -> resignGame();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (Exception e) {
            return (e).getMessage();
        }
    }

    private String makeMove(String[] param) throws ResponseException {
        if(param.length >= 2){
            var startPositionChar = param[0];
            var endPositionChar = param[1];
            ChessPiece.PieceType promotionPiece = null;

            ChessPosition startPosition = convertToPosition(startPositionChar);
            ChessPosition endPosition = convertToPosition(endPositionChar);

            if(param.length >= 3){
                var pieceToPromote = param[3];

                promotionPiece = switch (pieceToPromote) {
                    case "q" -> ChessPiece.PieceType.QUEEN;
                    case "b" -> ChessPiece.PieceType.BISHOP;
                    case "r" -> ChessPiece.PieceType.ROOK;
                    case "n" -> ChessPiece.PieceType.KNIGHT;
                    case null, default -> throw new ResponseException(400, "Expected promotion piece (q,b,r,n)");
                };
            }

            ChessMove move = new ChessMove(startPosition, endPosition, promotionPiece);

            ws = new WebSocketFacade(serverURL, notificationHandler);
            ws.makeMove(username1, move);

        }

        throw new ResponseException(400, "Expected <STARTPOSITION> <ENDPOSITION>");
    }

    private String highlightMoves(String[] param) throws ResponseException {
        if(param.length >= 1){
            var input = param[0];
            ChessPosition position = convertToPosition(input);

            printBoard = new PrintBoard(game.game());

            printBoard.printBoard(userColor, position);

            return "Possible moves for piece at " + input;

        }

        throw new ResponseException(400, "Expected <POSITION>");
    }

    private int convertToInteger(char input) throws ResponseException {
        int row;

        if(input == 'a'){
            row = 0;
        } else if (input == 'b') {
            row = 1;
        }else if (input == 'c') {
            row = 2;
        }else if (input == 'd') {
            row = 3;
        }else if (input == 'e') {
            row = 4;
        }else if (input == 'f') {
            row = 5;
        }else if (input == 'g') {
            row = 6;
        }else if (input == 'h') {
            row = 7;
        }else{
            throw new ResponseException(400, "Invalid position");
        }

        return row;
    }

    private int correctCol(char input) throws ResponseException {
        int col;

        col = (int) input - 1;

        if(col >= 8){
            throw new ResponseException(400, "Invalid position");
        }

        return col;
    }

    private ChessPosition convertToPosition(String input) throws ResponseException {
        ChessPosition position;
        int row;
        int col;
        char[] charOfPosition = input.toCharArray();

        row = convertToInteger(charOfPosition[0]);

        col = correctCol(charOfPosition[1]);

        position = new ChessPosition(row, col);

        return position;
    }

    private String observeGame(String[] param) throws ResponseException, URISyntaxException {
        if(param.length >= 1){
            var gameID = Integer.parseInt(param[0]);
            int gameToJoin = 0;
            int count = 1;
            for(var game : games){
                if(count == gameID){
                    gameToJoin = game.gameID();
                }
                count ++;
            }

            if(gameToJoin == 0){
                return "Game does not exist";
            }

            state = State.GAMEPLAY;
            ws = new WebSocketFacade(serverURL, notificationHandler);
            ws.observeGame(username1, gameID);

            game = ws.getGame();
            printBoard = new PrintBoard(game.game());

            printBoard.printBoard(userColor, null);

            return "Observing game " + gameID;
        }

        throw new ResponseException(400, "Expected <ID>");
    }




    private String resignGame() throws ResponseException {
        ws = new WebSocketFacade(serverURL, notificationHandler);
        ws.resignGame(username1);

        return username1 + " has resigned the game";
    }

    private String leaveGame() throws ResponseException {
        ws = new WebSocketFacade(serverURL, notificationHandler);
        ws.leaveGame(username1);

        return username1 + " has left the game";
    }

    private String redrawBoard() throws ResponseException {
        ws = new WebSocketFacade(serverURL, notificationHandler);
        GameData game = ws.getGame();
        printBoard = new PrintBoard(game.game());
        if(username1.equals(game.blackUsername())){
            userColor = ChessGame.TeamColor.BLACK;
        }

        printBoard.printBoard(userColor, null);

        return "board redrawn";
    }

    private String joinGame(String[] param) throws ResponseException, URISyntaxException {
        if(param.length >= 2){
            var gameID = Integer.parseInt(param[0]);
            var teamColor = param[1].toUpperCase();
            int gameToJoin = 0;
            int count = 1;
            for(var game : games){
                if(count == gameID){
                    gameToJoin = game.gameID();
                }
                count ++;
            }

            if(gameToJoin == 0){
                return "Game does not exist";
            }

            server.joinGame(gameToJoin, teamColor);
            state = State.GAMEPLAY;
            ws = new WebSocketFacade(serverURL, notificationHandler);
            ws.joinGame(username1, gameID);

            game = ws.getGame();
            printBoard = new PrintBoard(game.game());
            if(username1.equals(game.blackUsername())){
                userColor = ChessGame.TeamColor.BLACK;
            }

            printBoard.printBoard(userColor, null);


            return "joined game " + gameID;
        }

        throw new ResponseException(400, "Expected <ID> [WHITE|BLACK]");
    }

    private String listGames() {
        games = server.listGames();
        var result = new StringBuilder();
        int count = 1;

        for(var game : games){
            result.append(count).append(") Game Name: ").append(game.gameName()).append("\n");
            if(game.whiteUsername() != null){
                result.append("White Username: ").append(game.whiteUsername()).append("\n");
            }
            if(game.blackUsername() != null){
                result.append("Black Username: ").append(game.blackUsername()).append("\n");
            }
            result.append("\n");

            count ++;

        }

       return result.toString();
    }

    private String logOut() {

        server.logOut();
        state = State.LOGGEDOUT;

        return "Logged out\n";
    }

    private String registerUser(String... param) throws ResponseException {
        if(param.length >= 3){
            var username = param[0];
            var password = param[1];
            var email = param[2];
            var newUser = new UserData(username, password, email);
            username1 = username;
            server.registerUser(newUser);
            state = State.LOGGEDIN;

            return newUser.username() + " is now registered\n";
        }

        throw new ResponseException(400, "Expected <USERNAME> <PASSWORD> <EMAIL>");
    }

    private String logIn(String... param) throws ResponseException {
        if(param.length >= 2){
            var username = param[0];
            var password = param[1];
            UserData user = new UserData(username, password, null);
            username1 = username;
            server.logIn(user);
            state = State.LOGGEDIN;

            return user.username() + " is now logged in\n";
        }

        throw new ResponseException(400, "Expected <USERNAME> <PASSWORD>");
    }

    private String createGame(String ... param) throws ResponseException {
        if(param.length >= 1){
            var gameName = param[0];
            GameData newGame = new GameData(0, "n", "n", gameName, null);

            server.createGame(newGame);

            return " Game " + gameName + " is now created\n";
        }

        throw new ResponseException(400, "Expected <NAME>");

    }
}
