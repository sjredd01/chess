package dataaccess;

import exception.ResponseException;
import model.AuthData;
import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySQLUserDAO implements UserDAO{
    public MySQLUserDAO() throws ResponseException, DataAccessException{
        configureDatabase();
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  User (
              `username` varchar(256) NOT NULL UNIQUE,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              INDEX(username),
              INDEX(email)
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

    private void executeUpdate(String statement, Object... params) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    switch (param) {
                        case String p -> ps.setString(i + 1, p);
                        case AuthData p -> ps.setString(i + 1, p.toString());
                        case null -> ps.setNull(i + 1, NULL);

                        default -> throw new IllegalStateException("Unexpected value: " + param);
                    }
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    rs.getInt(1);
                }

            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private UserData readUser(ResultSet rs) throws SQLException {
        var userName = rs.getString("username");
        var password = rs.getString("password");
        var email = rs.getString("email");
        return new UserData(userName, password, email);
    }

    @Override
    public void createUser(UserData user) throws DataAccessException, ResponseException {
        try(var conn = DatabaseManager.getConnection()){
            var statement = "INSERT INTO User (username, password, email) VALUES (?, ?, ?)";
            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

            executeUpdate(statement, user.username(), hashedPassword, user.email());
        }catch (SQLException e){
            throw new DataAccessException("User already exists: " + user.username());
        }

    }

    @Override
    public UserData getUser(String username) throws DataAccessException, ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM User WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        throw new DataAccessException("Username doesn't exist");
    }

    @Override
    public boolean checkUser(String username, String password) throws DataAccessException, ResponseException {
        UserData user = getUser(username);

        return BCrypt.checkpw(password, user.password());
    }

    @Override
    public void clear() throws ResponseException {
        var statement = "TRUNCATE TABLE User";
        executeUpdate(statement);

    }
}
