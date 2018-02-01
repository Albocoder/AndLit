package albocoder.github.com.facedetector.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import albocoder.github.com.facedetector.database.converters.DateConverter;

@Entity(tableName = "known_ppl",indices = {@Index(value = "global_id",
        unique = true)})
public class KnownPPL {
    @PrimaryKey(autoGenerate=true)
    public int id;
    public long global_id;
    public String name;
    public String sname;
    public long dob;
    public int age;
    public String address;

    public KnownPPL(long global_id,String name,String sname,long dob, int age, String address){
        this.global_id = global_id;
        this.name = name;
        this.sname = sname;
        this.dob = dob;
        this.age = age;
        this.address = address;
    }

    public KnownPPL(long gid,String n,String s,Date d, int ag, String ad){
        this(gid,n,s,DateConverter.toTimestamp(d),ag,ad);
    }
}
