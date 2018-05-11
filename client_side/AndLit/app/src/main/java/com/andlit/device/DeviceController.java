package com.andlit.device;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.andlit.HomeActivity;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.AndLitDevice;
import com.andlit.database.entities.AndLitDeviceConnection;
import com.andlit.database.entities.WifiCredentials;
import com.jcraft.jsch.JSchException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DeviceController {
    public SSHInterface conn;

    public DeviceController(AndLitDevice d) throws JSchException {
        conn = new SSHInterface(d.username,d.password,d.ip);
    }

    public SSHInterface getConnection(){ return conn; }

    // todo: add this to view (its not part of a controller)
//    public static List<String> getWifiNames(Context c) {
//        final WifiManager wifiManager = (WifiManager) c.getApplicationContext()
//                .getSystemService(Context.WIFI_SERVICE);
//        if(wifiManager == null)
//            return new ArrayList<>(0);
//
//        BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context c, Intent intent) {
//                if (intent.getAction() != null &&
//                        intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
//                    LinkedList<String> toReturn = new LinkedList<>();
//                    List<ScanResult> scanResults = wifiManager.getScanResults();
//                    for (ScanResult tmp: scanResults)
//                        toReturn.add(tmp.SSID);
//                    Log.d("WIFISCAN",toReturn.toString());
//                }
//            }
//        };
//        c.registerReceiver(mWifiScanReceiver,
//                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//        wifiManager.startScan();
//        return null;
//    }

    public static long registerDevice(AndLitDevice d,Context c){
        AppDatabase db = AppDatabase.getDatabase(c);
        return db.andlitDeviceDao().insertAndLitDevice(d);
    }

    public static long registerDeviceWithConnection(AndLitDevice d,
                                                       WifiCredentials creds, Context c){
        AppDatabase db = AppDatabase.getDatabase(c);
        Long id =  db.andlitDeviceDao().insertAndLitDevice(d);
        db.wifiCredentialsDao().insertCredentials(creds);
        db.andlitDeviceConnection().insertConnection(new AndLitDeviceConnection(id,creds.ssid));
        return id;
    }
}
