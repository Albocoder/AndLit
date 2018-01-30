package albocoder.github.com.facedetector.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "user_login")
public class UserLogin {
    @PrimaryKey
    public final long id;
    public String username;
    public String access_token;

    public UserLogin(long id,String un,String acc_tok) {
        this.id = id;
        username = un;
        access_token = acc_tok;
    }
}
