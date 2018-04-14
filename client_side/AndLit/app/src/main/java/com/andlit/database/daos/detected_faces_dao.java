package com.andlit.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.andlit.database.entities.detected_face;

import java.util.List;


@Dao
public interface detected_faces_dao {
    @Insert
    void insertFace(detected_face key);

    @Query("select * from detected_faces")
    List<detected_face> getAllRecords();

    @Query("select * from detected_faces where hash = :h")
    List<detected_face> getDetectionWithHash(String h);

    @Query("select distinct `id` from detected_faces")
    List<Integer> getAllIDsDetectedSoFar();

    @Query("delete from detected_faces")
    void purgeData();

    @Query("delete from detected_faces where `id` = :i")
    void deleteDetectionForID(int i);

    @Delete
    void deleteDetectionForFace(detected_face f);

    @Query("delete from detected_faces where `hash` = :h ")
    void deleteDetectionForHash(String h);

    @Query("delete from detected_faces where `date_taken` >= :s and `date_taken` <= :e")
    void deleteDetectionForTime(long s,long e);

    @Query("delete from detected_faces where `id` = :i and `date_taken` >= :s " +
            "and `date_taken` <= :e")
    void deleteDetectionForIDInTimeSpan(int i,long s,long e);

    @Update(onConflict = OnConflictStrategy.ROLLBACK)
    void updateRowData(detected_face toChange);
}
