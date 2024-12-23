package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import exception.ResponseException;
import model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;

public class ServerFacade {

    private final String serverURL;
    String authToken;

    public ServerFacade(String serverURL) {
        this.serverURL = serverURL;
    }

    private String getAuthToken() {
        return authToken;
    }

    private void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String registerUser(UserData newUser) {
        var path = "/user";
        var request = makeRequest("POST", path, newUser, AuthData.class);
        setAuthToken(request.authToken());

        return authToken;
    }

    public String logIn(UserData user) {
        var path = "/session";
        var request = makeRequest("POST", path, user, AuthData.class);
        setAuthToken(request.authToken());
        return authToken;
    }

    public int createGame(GameData gameName){
        var path = "/game";
        var request = makeRequest("POST", path, gameName, GameData.class);
        return request.gameID();
    }

    public boolean logOut(){
        var path = "/session";
        this.makeRequest("DELETE", path, authToken, null);
        return true;
    }

    public ArrayList<GameDataList> listGames() {
        var path = "/game";
        record ListGames(ArrayList<GameDataList> games){

        }
        var request = this.makeRequest("GET", path, null, ListGames.class);
       return request.games;
    }

    public void joinGame(int gameID, String teamColor) {
        var path = "/game";
        JoinGameRequest joinRequest = new JoinGameRequest(teamColor, gameID);
        this.makeRequest("PUT", path, joinRequest, null );

    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) {
        try{
            URL url = (new URI(serverURL + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            if (authToken != null) {
                http.addRequestProperty("Authorization", authToken);
            }
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (URISyntaxException | IOException | ResponseException e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    private <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if(http.getContentLength() < 0){
            try (InputStream resBody = http.getInputStream()){
                InputStreamReader reader = new InputStreamReader(resBody);
               if(responseClass != null){
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }

        return response;
    }

    private void writeBody(Object request, HttpURLConnection http) throws IOException {
        if(request != null){
            http.addRequestProperty("Content-Type", "application/json");
            String data = new Gson().toJson(request);
            try(OutputStream body = http.getOutputStream()){
                body.write(data.getBytes());
            }

        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws ResponseException, IOException {
        var status = http.getResponseCode();
        String error = "";
        if(status == 401){
            error = "Unauthorized";
        }
        if(status == 403){
            error = "User already taken";
        }
        if(status == 400){
            error = "Bad request";
        }


        if (!isSuccessful(status)) {
            throw new ResponseException(status, error);
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }



}
