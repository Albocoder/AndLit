package albocoder.github.com.facedetector.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import albocoder.github.com.facedetector.database.entities.UserLogin;

@Dao
public interface user_login_dao {
    @Insert
    void insertEntry(UserLogin l);

    @Query("select * from user_login")
    List<UserLogin> getLoginEntry();

    @Query("delete from user_login")
    void deleteEntries();

    @Update(onConflict = OnConflictStrategy.ROLLBACK)
    void updateEntry(UserLogin toChange);
}
