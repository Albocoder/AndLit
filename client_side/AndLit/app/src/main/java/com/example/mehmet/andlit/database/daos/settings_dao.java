package com.example.mehmet.andlit.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.mehmet.andlit.database.entities.Setting;

import java.util.List;

@Dao
public interface settings_dao {
    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    void insertSetting(Setting s);

    @Query("select * from settings where `key`= :k")
    Setting getSetttingWithKey(String k);

    @Query("select * from settings where value= :v")
    List<Setting> getSettingsWithValue(String v);

    @Query("delete from settings")
    void purgeSettings();

    @Delete
    void deleteSetting(Setting s);

    @Update
    void updateSetting(Setting s);
}
