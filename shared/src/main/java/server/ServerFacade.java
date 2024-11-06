package server;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;

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

    public AuthData registerUser(UserData newUser) {
        var path = "/user";
        setAuthToken(makeRequest("POST", path, newUser, AuthData.class).authToken());
        return this.makeRequest("POST", path, newUser, AuthData.class);
    }

    public AuthData logIn(UserData user) {
        var path = "/session";
        setAuthToken(makeRequest("POST", path, user, AuthData.class).authToken());
        return this.makeRequest("POST", path, user, AuthData.class);
    }

    public void createGame(String gameName){
        var path = "/game";
        this.makeRequest("POST", path, gameName, String.class);
    }

    public UserData logOut(UserData user){
        var path = "/session";
        return this.makeRequest("DELETE", path, user, UserData.class);
    }



    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) {
        try{
            URL url = (new URI(serverURL + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            if (authToken != null && !authToken.isEmpty()) {
                http.addRequestProperty("Authorization", authToken);
                writeBody(request, http);
                http.connect();

                return null;
            }

            writeBody(request, http);
            http.connect();
            return readBody(http, responseClass);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
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


}
