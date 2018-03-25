package com.andlit.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.andlit.database.entities.misc_info;

import java.util.List;



@Dao
public interface misc_info_dao {
    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    void insertMiscInfos(misc_info... mis);

    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    void insertMiscInfo(misc_info mi);

    @Query("select * from misc_info")
    List<misc_info> getAllRecords();

    @Query("select distinct `key` from misc_info")
    List<String> getAllKeys();

    @Query("select distinct id from misc_info")
    List<Integer> getAllIDsHavingInfo();

    @Query("select * from misc_info where id= :i")
    List<misc_info> getInfosForID(int i);

    @Query("select `key` from misc_info where id= :i")
    List<String> getKeysForID(int i);

    @Query("delete from misc_info")
    void purgeData();

    @Query("delete from misc_info where `id` = :id")
    void deleteDataForID(int id);

    @Query("delete from misc_info where `key` = :k")
    void deleteKeys(String k);

    @Query("delete from misc_info where `key` = :k and `id` = :id")
    void deleteKeyForID(String k,int id);

    @Delete
    void deleteEntry(misc_info toDelete);

    @Delete
    void deleteEntries(misc_info... toDelete);

   @Update(onConflict = OnConflictStrategy.ROLLBACK)
    void updateRowData(misc_info toChange);
}
