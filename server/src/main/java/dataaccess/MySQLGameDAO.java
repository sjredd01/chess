package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import exception.ResponseException;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySQLGameDAO implements GameDAO{

    public MySQLGameDAO() throws ResponseException, DataAccessException{
        configureDatabase3();
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS Game (
              `gameID` int NOT NULL,
              `whiteUsername` varchar(256),
              `blackUsername` varchar(256),
              `gameName` varchar(256) NOT NULL,
              `game` TEXT DEFAULT NULL,
              INDEX(gameID),
              INDEX(gameName)
            )
            """
    };

    private void configureDatabase3() throws ResponseException, DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn3 = DatabaseManager.getConnection()) {
            for (var statement3 : createStatements) {
                try (var preparedStatement3 = conn3.prepareStatement(statement3)) {
                    preparedStatement3.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new ResponseException(500, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }

    private void executeUpdate(String statement, Object... params) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps2 = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    switch (param) {
                        case String p -> ps2.setString(i + 1, p);
                        case Integer p -> ps2.setInt(i + 1, p);
                        case JsonElement p -> ps2.setString(i + 1, p.toString());
                        case null -> ps2.setNull(i + 1, NULL);
                        default -> {
                        }
                    }
                }
                ps2.executeUpdate();

                var rs = ps2.getGeneratedKeys();
                if (rs.next()) {
                    rs.getInt(1);
                }

            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        var gameID = rs.getInt("gameID");
        var whiteUserName = rs.getString("whiteUsername");
        var blackUserName = rs.getString("blackUsername");
        var gameName = rs.getString("gameName");
        var json = rs.getString("game");
        var game = new Gson().fromJson(json, ChessGame.class);

        return new GameData(gameID, whiteUserName, blackUserName, gameName, game);
    }





    public void removeGame(int gameID) throws ResponseException {
        var statement = "DELETE FROM Game WHERE gameID=?";
        executeUpdate(statement, gameID);
    }

    @Override
    public HashSet<GameData> listGames() throws ResponseException {
        var result = new HashSet<GameData>();

        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM Game";
            try (var ps = conn.prepareStatement(statement);
                 var rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(readGame(rs));
                }
            }
        } catch (SQLException e) {
            // Log the SQL exception (optional)
            throw new ResponseException(500, String.format("Database error: %s", e.getMessage()));
        } catch (Exception e) {
            // General exception handling
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }

        return result;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException, ResponseException {

        try(var conn = DatabaseManager.getConnection()){
            var statement = "INSERT INTO Game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
            var json = new Gson().toJson(game.game());

            executeUpdate(statement, game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), json);
        }catch (SQLException e){
            throw new DataAccessException("Game already exists: " + game.gameName());
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException, ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM Game WHERE gameID=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameId);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        throw new DataAccessException("Game doesn't exist");
    }

    @Override
    public boolean gameExists(int gameId) throws ResponseException, DataAccessException {
        try{
            getGame(gameId);
        }catch (NullPointerException e){
            return false;
        }
        return true;

    }

    @Override
    public void updateGame(GameData game) throws DataAccessException{

        try{
            removeGame(game.gameID());
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }

        try {
           createGame(game);
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void clear() throws ResponseException {
        var statement = "TRUNCATE TABLE Game";
        executeUpdate(statement);

    }
}
