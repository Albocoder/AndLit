package albocoder.github.com.facedetector.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "api_keys")
public class API_key {
    @PrimaryKey @NonNull
    public String website_name;
    public String key;

    public API_key(String website_name,String key){
        this.website_name = website_name==null?"":website_name;
        this.key = key;
    }
}
