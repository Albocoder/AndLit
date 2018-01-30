package albocoder.github.com.facedetector.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "known_ppl",indices = {@Index(value = "global_id",
        unique = true)})
public class KnownPPL {
    @PrimaryKey(autoGenerate=true)
    public int id;
    public long global_id;
    public String name;
    public String sname;
    public Date dob;
    public int age;
    public String address;

    public KnownPPL(long gid,String n,String s,Date d, int ag, String ad){
        global_id = gid;
        name = n;
        sname = s;
        dob = d;
        age = ag;
        address = ad;
    }
}
