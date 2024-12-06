package ui;

import chess.*;
import exception.ResponseException;
import model.GameData;
import model.GameDataList;
import model.UserData;
import server.ServerFacade;
import ui.websocket.NotificationHandler;
import ui.websocket.WebSocketFacade;

import java.net.URISyntaxException;
import java.util.ArrayList;
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
    private ArrayList<GameDataList> games;
    public static PrintBoard printBoard;
    private ChessGame.TeamColor userColor = ChessGame.TeamColor.WHITE;
    private ChessBoard game;
    private String authToken;
    private int currentGameID;

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
            ws.makeMove(authToken, move, currentGameID);
            return "";
        }
        else {
            throw new ResponseException(400, "Expected <STARTPOSITION> <ENDPOSITION>");
        }
    }

    private String highlightMoves(String[] param) throws ResponseException {
        if(param.length >= 1){
            var input = param[0];
            ChessPosition position = convertToPosition(input);

            printBoard = new PrintBoard(game);

            printBoard.printBoard(userColor, position);

            return "Possible moves for piece at " + input;

        }

        throw new ResponseException(400, "Expected <POSITION>");
    }


    private ChessPosition convertToPosition(String input) throws ResponseException {
        var columnLetter =Character.toUpperCase(input.charAt(0));
        int columnNum = (int) columnLetter - (int) 'A' + 1;

        if(columnNum < 1 || columnNum > 8) {
            throw new ResponseException(400, "Invalid Coordinates");
        }

        var rowNum = Character.getNumericValue(input.charAt(1));
        return new ChessPosition(rowNum, columnNum);
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
            ws.observeGame(authToken, gameToJoin);

            game = ws.getGame();
            printBoard = new PrintBoard(game);

            printBoard.printBoard(userColor, null);

            return "Observing game " + gameID;
        }

        throw new ResponseException(400, "Expected <ID>");
    }




    private String resignGame() throws ResponseException {
        ws = new WebSocketFacade(serverURL, notificationHandler);
        ws.resignGame(authToken, currentGameID);
        state = State.LOGGEDIN;

        return "";
    }

    private String leaveGame() throws ResponseException {
        ws = new WebSocketFacade(serverURL, notificationHandler);
        ws.leaveGame(authToken, currentGameID);
        state = State.LOGGEDIN;

        return "";
    }

    private String redrawBoard() throws ResponseException {
        ws = new WebSocketFacade(serverURL, notificationHandler);
        ChessBoard game = ws.getGame();
        printBoard = new PrintBoard(game);

        printBoard.printBoard(userColor, null);

        return "board redrawn";
    }

    private String joinGame(String[] param) throws ResponseException, URISyntaxException {
        if(param.length >= 2){
            var gameIndex = Integer.parseInt(param[0]);
            var teamColor = param[1].toUpperCase();

            if(teamColor.equals("BLACK")){
                userColor = ChessGame.TeamColor.BLACK;
            }

            var gameID = games.get(gameIndex - 1).gameID();

            currentGameID = gameID;

            server.joinGame(gameID, teamColor);
            state = State.GAMEPLAY;
            ws = new WebSocketFacade(serverURL, notificationHandler);
            ws.joinGame(authToken, gameID);

            game = ws.getGame();
            printBoard = new PrintBoard(game);

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
            authToken = server.registerUser(newUser);
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
            authToken= server.logIn(user);
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

    public ChessGame.TeamColor getUserColor() {
        return userColor;
    }
}
