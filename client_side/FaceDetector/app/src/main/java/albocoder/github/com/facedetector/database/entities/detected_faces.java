package albocoder.github.com.facedetector.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import albocoder.github.com.facedetector.database.converters.DateConverter;

@Entity(foreignKeys = {@ForeignKey(entity = KnownPPL.class,
        childColumns = "id",parentColumns = "id",onDelete = ForeignKey.CASCADE)})
public class detected_faces {
    @PrimaryKey
    public int id;
    public String path;
    public String hash;
    public long date_taken;

    public detected_faces(int id,String path,String hash,long date_taken){
        this.id = id;
        this.path = path;
        this.hash = hash;
        this.date_taken = date_taken;
    }

    public detected_faces(int id,String path,String hash,Date date_taken){
        this(id,path,hash, DateConverter.toTimestamp(date_taken));
    }
}
