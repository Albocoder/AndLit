package albocoder.github.com.facedetector.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

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

    @Query("select distinct `id` from detected_faces")
    List<Integer> getAllIDsDetectedSoFar();

    @Query("delete from detected_faces")
    void purgeData();

    @Query("delete from detected_faces where `id` = :i")
    void deleteDetectionForID(int i);

    @Delete
    void deleteDetectionForFace(detected_faces f);

    @Query("delete from detected_faces where `hash` = :h ")
    void deleteDetectionForHash(String h);

    @Query("delete from detected_faces where `date_taken` >= :s and `date_taken` <= :e")
    void deleteDetectionForTime(long s,long e);

    @Query("delete from detected_faces where `id` = :i and `date_taken` >= :s " +
            "and `date_taken` <= :e")
    void deleteDetectionForIDInTimeSpan(int i,long s,long e);

    @Update(onConflict = OnConflictStrategy.ROLLBACK)
    void updateRowData(detected_faces toChange);
}
