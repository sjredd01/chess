package dataaccess;

import exception.ResponseException;
import model.AuthData;

import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySQLAuthDAO implements AuthDAO{
    public MySQLAuthDAO() throws ResponseException, DataAccessException {
        configureDatabase2();
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  Auth (
              `authToken` varchar(256) NOT NULL,
              `username` varchar(256) NOT NULL,
              INDEX(authToken),
              INDEX(username)
            )
            """
    };

    private void configureDatabase2() throws ResponseException, DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn2 = DatabaseManager.getConnection()) {
            for (var statement2 : createStatements) {
                try (var preparedStatement2 = conn2.prepareStatement(statement2)) {
                    preparedStatement2.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new ResponseException(500, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }

    private void executeUpdate2(String statement, Object... params) throws ResponseException {
        try (var conn2 = DatabaseManager.getConnection()) {
            try (var ps2 = conn2.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param2 = params[i];
                    switch (param2) {
                        case String p -> ps2.setString(i + 1, p);
                        case AuthData p -> ps2.setString(i + 1, p.toString());
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


    @Override
    public void createAuth(AuthData authData) throws ResponseException {
        if (authData == null || authData.authToken() == null || authData.username() == null) {
            throw new ResponseException(400, "authToken and username cannot be null.");
        }
        var statement = "INSERT INTO Auth (authToken, username) VALUES (?, ?)";
        executeUpdate2(statement, authData.authToken(), authData.username());

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
        if (authToken == null || authToken.isEmpty()) {
            throw new ResponseException(400, "authToken cannot be null or empty.");
        }
        var statement = "DELETE FROM Auth WHERE authToken = ?";
        try {
            executeUpdate2(statement, authToken);
        } catch (ResponseException e) {
            throw new ResponseException(500, String.format("Unable to delete authToken %s: %s", authToken, e.getMessage()));
        }
    }

    @Override
    public void clear() throws ResponseException {
        var statement = "TRUNCATE TABLE Auth";
        executeUpdate2(statement);

    }
}
