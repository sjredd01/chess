package service;

import dataaccess.*;
import exception.ResponseException;
import model.AuthData;
import model.UserData;

import java.security.SecureRandom;
import java.sql.SQLException;

public class UserService extends AdminService{
    public UserService(GameDAO gameDAO, AuthDAO authDAO, UserDAO userDAO) {
        super(gameDAO, authDAO, userDAO);
    }

    public AuthData createNewUser(String username, String password, String email) throws DataAccessException, ResponseException, SQLException {
        String newAuthTokenString = makeAuthToken();

        try{
            userDAO.getUser(username);
        } catch (DataAccessException e) {
            UserData newUser = new UserData(username, password, email);
            userDAO.createUser(newUser);
            authDAO.createAuth(new AuthData(newAuthTokenString, username));
            return new AuthData(newAuthTokenString, username);
        }

        throw new RuntimeException("Error already taken");

    }

    public String loginUser(String username, String password) throws DataAccessException, ResponseException {

        if(userDAO.checkUser(username,password)){
            String newAuthToken = makeAuthToken();
            AuthData authData = new AuthData(newAuthToken, username);
            authDAO.createAuth(authData);

            return authData.authToken();
        }else{
            throw new DataAccessException("");
        }

    }

    public void logoutUser(String authToken) throws DataAccessException, ResponseException {
        if(authDAO.getAuth(authToken) != null){
            authDAO.deleteAuth(authToken);
        }else{
            throw new DataAccessException("unauthorized");
        }

    }

    public String makeAuthToken(){
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        final int stringLength = 10;
        SecureRandom random = new SecureRandom();
        StringBuilder newAuthToken = new StringBuilder(stringLength);


        for (int i = 0; i < stringLength; i++) {
            int randomIndex = random.nextInt(characters.length());
            newAuthToken.append(characters.charAt(randomIndex));
        }

        return newAuthToken.toString();

    }


}
