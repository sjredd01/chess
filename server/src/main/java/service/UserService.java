package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

import java.security.SecureRandom;

public class UserService extends AdminService{
    public UserService(GameDAO gameDAO, AuthDAO authDAO, UserDAO userDAO) {
        super(gameDAO, authDAO, userDAO);
    }

    public AuthData createNewUser(String username, String password, String email) throws DataAccessException {
        String newAuthTokenString = makeAuthToken();

        try{
            userDAO.getUser(username);
        } catch (DataAccessException e) {
            UserData newUser = new UserData(username, password, email);
            userDAO.createUser(newUser);
            return new AuthData(newAuthTokenString, username);
        }

        throw new RuntimeException("Error already taken");

    }

    public String loginUser(String username, String password) throws DataAccessException {

        if(userDAO.checkUser(username,password)){
            String newAuthToken = makeAuthToken();
            AuthData authData = new AuthData(newAuthToken, username);
            authDAO.createAuth(authData);

            return authData.authToken();
        }else{
            throw new DataAccessException("");
        }

    }

    public void logoutUser(String authToken) throws DataAccessException {
        if(authDAO.getAuth(authToken) != null){
            authDAO.deleteAuth(authToken);
        }else{
            throw new DataAccessException("unauthorized");
        }

    }

    public String makeAuthToken(){
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        final int STRING_LENGTH = 10;
        SecureRandom random = new SecureRandom();
        StringBuilder newAuthToken = new StringBuilder(STRING_LENGTH);


        for (int i = 0; i < STRING_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            newAuthToken.append(CHARACTERS.charAt(randomIndex));
        }

        return newAuthToken.toString();

    }


}
