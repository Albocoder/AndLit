package albocoder.github.com.facedetector.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "user_login")
public class UserLogin {
    @PrimaryKey
    public final Long id;
    public String username;
    public String access_token;

    public UserLogin(long id,String username,String access_token) {
        this.id = id;
        this.username = username;
        this.access_token = access_token;
    }

    @Override
    public String toString() {
        return "UserTuple: < `"+id+"`, `"+username+"`, `"+access_token+"` >";
    }
}
