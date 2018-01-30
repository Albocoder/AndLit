package albocoder.github.com.facedetector.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "api_keys")
public class API_key {
    @PrimaryKey
    public String website_name;
    public String key;

    public API_key(String name,String key){
        website_name = name;
        this.key = key;
    }
}
