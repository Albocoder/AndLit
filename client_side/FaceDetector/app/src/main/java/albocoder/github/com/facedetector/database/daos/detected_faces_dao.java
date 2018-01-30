package albocoder.github.com.facedetector.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import albocoder.github.com.facedetector.database.entities.detected_faces;

@Dao
public interface detected_faces_dao {
    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    void insertFaces(detected_faces... f);

    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    void insertFace(detected_faces key);

    @Query("select * from detected_faces")
    List<detected_faces> getAllRecords();

    // Todo: Continue from here
    @Query("select distinct `id` from detected_faces")
    List<Integer> getKeyForWebsite();

    @Query("select distinct website_name from api_keys where `key` = :k")
    List<String> getWebsitesHavingKey(String k);

    @Query("delete from api_keys")
    void purgeData();

    @Delete
    void deleteKey(detected_faces f);

    @Delete
    void deleteKeys(detected_faces... f);
}
