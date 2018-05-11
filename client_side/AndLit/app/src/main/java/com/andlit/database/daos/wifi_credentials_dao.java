package com.andlit.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.andlit.database.entities.WifiCredentials;

@Dao
public interface wifi_credentials_dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCredentials(WifiCredentials c);

    @Update(onConflict = OnConflictStrategy.ROLLBACK)
    void updateCredentials(WifiCredentials c);

    @Delete
    void deleteCredentials(WifiCredentials c);

    @Query("select * from wifi_credentials where ssid = :s")
    WifiCredentials getWirelessCredentialsForSSID(String s);
}
