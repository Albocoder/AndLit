package com.andlit.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.andlit.database.entities.AndLitDevice;

import java.util.List;

@Dao
public interface andlit_device_dao {
    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    Long insertAndLitDevice(AndLitDevice d);

    @Update(onConflict = OnConflictStrategy.ROLLBACK)
    void updateAndLitDevice(AndLitDevice d);

    @Delete
    void deleteAndLitDevice(AndLitDevice d);

    @Query("select * from andlit_devices")
    List<AndLitDevice> getAllDevices();

    @Query("select * from andlit_devices where username = :u")
    List<AndLitDevice> getDevicesWithUsername(String u);
}
