package com.andlit.device;

import android.content.Context;

import com.andlit.database.entities.AndLitDevice;
import com.andlit.database.entities.AndLitDeviceConnection;

import com.jcraft.jsch.JSchException;

public class DeviceController {
    private SSHInterface conn;

    public DeviceController(Context c, AndLitDevice d, AndLitDeviceConnection adc)
            throws JSchException {
        conn = new SSHInterface(d.username,d.password,adc.ip);
    }

    public DeviceController(Context c) throws JSchException {
        conn = new SSHInterface();
    }

    public SSHInterface getConnection(){ return conn; }

//    public static long registerDevice(AndLitDevice d,Context c){
//        AppDatabase db = AppDatabase.getDatabase(c);
//        return db.andlitDeviceDao().insertAndLitDevice(d);
//    }

//    public static long registerDeviceWithConnection(AndLitDevice d,
//                                                       WifiCredentials creds, Context c){
//        AppDatabase db = AppDatabase.getDatabase(c);
//        Long id =  db.andlitDeviceDao().insertAndLitDevice(d);
//        db.wifiCredentialsDao().insertCredentials(creds);
//        db.andlitDeviceConnection().insertConnection(new AndLitDeviceConnection(id,creds.ssid));
//        return id;
//    }
}
