package dataaccess;

import exception.ResponseException;
import model.AuthData;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collection;
import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySQLAuthDAO implements AuthDAO{
    public MySQLAuthDAO() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  Auth (
              `authToken` varchar(256) NOT NULL,
              `username` varchar(256) NOT NULL,
              INDEX(authToken),
              INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws ResponseException, DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new ResponseException(500, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }

    private int executeUpdate(String statement, Object... params) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    switch (param) {
                        case String p -> ps.setString(i + 1, p);
                        case AuthData p -> ps.setString(i + 1, p.toString());
                        case null -> ps.setNull(i + 1, NULL);
                        default -> {
                        }
                    }
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }


    @Override
    public void createAuth(AuthData authData) throws ResponseException {
        var statement = "INSERT INTO Auth (authToken, username) VALUES (?, ?)";
        executeUpdate(statement, authData.authToken(), authData.username());
        
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException, ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM Auth WHERE authToken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        throw new DataAccessException("Auth Token doesn't exist");
    }

    private AuthData readAuth(ResultSet rs) throws SQLException {
        var authToken = rs.getString("authToken");
        var userName = rs.getString("username");
        return new AuthData(authToken, userName);
    }

    @Override
    public void deleteAuth(String authToken) throws ResponseException {
        var statement = "DELETE FROM Auth WHERE authToken = ?";
        try {
            executeUpdate(statement, authToken);
        } catch (ResponseException e) {
            throw new ResponseException(500, String.format("Unable to delete authToken %s: %s", authToken, e.getMessage()));
        }
    }

    @Override
    public void clear() throws ResponseException {
        var statement = "TRUNCATE TABLE Auth";
        executeUpdate(statement);

    }
}