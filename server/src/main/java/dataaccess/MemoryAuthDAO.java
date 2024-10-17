package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashSet;

public class MemoryAuthDAO implements AuthDAO{

    private HashSet<AuthData> db;

    public MemoryAuthDAO(){
        db = HashSet.newHashSet(10);
    }

    @Override
    public void createAuth(AuthData authData) {
        db.add(authData);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        for(AuthData authData : db){
            if(authData.authToken().equals(authToken)){
                return authData;
            }
        }

        throw new DataAccessException("Auth Token doesn't exist");
    }

    @Override
    public void deleteAuth(String authToken) {
        for(AuthData authData : db){
            if(authData.authToken().equals(authToken)){
                db.remove(authData);
                break;
            }
        }

    }

    @Override
    public void clear() {
        db = HashSet.newHashSet(10);
    }
}
