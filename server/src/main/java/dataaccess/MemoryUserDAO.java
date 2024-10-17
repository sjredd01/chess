package dataaccess;
import java.util.HashSet;
import model.UserData;

public class MemoryUserDAO implements UserDAO {

    private HashSet<UserData> db;

    public MemoryUserDAO(){
        db = HashSet.newHashSet(10);
    }


    @Override
    public void createUser(UserData user) throws DataAccessException {
        try{
            getUser(user.username());
        } catch (DataAccessException e) {
            db.add(user);
            return;
        }

        throw  new DataAccessException("User already exists");
    }

    @Override
   public UserData getUser(String username) throws DataAccessException{
        for(UserData user : db){
            if(user.username().equals(username)){
                return user;
            }
        }

        throw new DataAccessException("User not found: " + username);
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        return false;
    }

    @Override
    public void clear() {
        db = HashSet.newHashSet(10);

    }
}