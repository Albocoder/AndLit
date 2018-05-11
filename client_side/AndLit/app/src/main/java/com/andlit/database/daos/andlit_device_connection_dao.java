package com.andlit.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.andlit.database.entities.AndLitDeviceConnection;

import java.util.List;

@Dao
public interface andlit_device_connection_dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertConnection(AndLitDeviceConnection adc);

    @Update(onConflict = OnConflictStrategy.ROLLBACK)
    void updateConnection(AndLitDeviceConnection adc);

    @Delete
    void deleteConnection(AndLitDeviceConnection adc);

    @Query("select * from andlit_device_connection where mac = :id")
    List<AndLitDeviceConnection> getAllConnectionsOfDevice(Integer id);

    @Query("select * from andlit_device_connection where ssid = :s")
    List<AndLitDeviceConnection> getAllConnectionOfWifi(String s);
}