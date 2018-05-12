package com.andlit.ui.andlitDevice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.andlit.R;
import com.andlit.device.SSHInterface;
import com.jcraft.jsch.JSchException;

public class BluetoothConfigActivity extends Activity {

    BluetoothSocket mmSocket;

    Spinner devicesSpinner;
    Button refreshDevicesButton;
    TextView ssidText;
    TextView pskTextView;
    Button startButton;

    WifiManager wifiManager;

    final UUID uuid = UUID.fromString("815425a5-bfac-47bf-9321-c5ff980b5e11");
    final byte delimiter = 33;
    int readBufferPosition = 0;

    private ProgressDialog settingUpConnection;
    private ProgressDialog connectingViaWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_config);

        settingUpConnection = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        settingUpConnection.setTitle("Configuring...");
        settingUpConnection.setMessage("Please wait!");
        settingUpConnection.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        settingUpConnection.setInverseBackgroundForced(false);
        settingUpConnection.setIndeterminate(true);
        settingUpConnection.setCancelable(true);

        connectingViaWifi = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        connectingViaWifi.setMessage("Connecting to the device");
        connectingViaWifi.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        connectingViaWifi.setInverseBackgroundForced(false);
        connectingViaWifi.setIndeterminate(true);
        connectingViaWifi.setCancelable(true);

        ssidText = findViewById(R.id.ssid_text);
        pskTextView = findViewById(R.id.psk_text);

        devicesSpinner = findViewById(R.id.devices_spinner);

        refreshDevicesButton = findViewById(R.id.refresh_devices_button);
        startButton = findViewById(R.id.start_button);

        refreshDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDevices();
                refreshAPs();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid = ssidText.getText().toString().substring(18);
                ssid = ssid.substring(0,ssid.length()-1);
                String psk = pskTextView.getText().toString();

                BluetoothDevice device = (BluetoothDevice) devicesSpinner.getSelectedItem();
                new WorkerThread(ssid, psk, device).execute();
                startButton.setEnabled(false);
            }
        });

        refreshDevices();
        refreshAPs();
    }

    private void refreshDevices() {
        DeviceAdapter adapter_devices = new DeviceAdapter(this, R.layout.spinner_devices, new ArrayList<BluetoothDevice>());
        devicesSpinner.setAdapter(adapter_devices);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapter_devices.add(device);
            }
        }
    }

    private void refreshAPs() {
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager == null) {
            return;
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getSSID()) &&
                wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED)) {
            ssidText.setText("Configured with: "+wifiInfo.getSSID());
            startButton.setEnabled(true);
        }
        else {
            ssidText.setText("Not connected to Wifi");
            startButton.setEnabled(false);
        }
//        BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context c, Intent intent) {
//                if (intent.getAction() != null &&
//                        intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
//                    WifiSSIDAdapter wifiSSIDAdapter = new WifiSSIDAdapter
//                            (BluetoothConfigActivity.this,R.layout.spinner_devices,
//                                    new ArrayList<String>());
//                    List<ScanResult> scanResults = wifiManager.getScanResults();
//                    for (ScanResult tmp: scanResults) {
//                        if(tmp.SSID.equals(""))
//                            continue;
//                        String capabilities = tmp.capabilities;
//                        if (capabilities.contains("WPA")||capabilities.contains("WEP")) {
//                            wifiSSIDAdapter.add("ENC-"+tmp.SSID);
//                        }
//                        else {
//                            wifiSSIDAdapter.add("DEC-"+tmp.SSID);
//                        }
//                    }
//                    ssidSpinner.setAdapter(wifiSSIDAdapter);
//                    wifiRefreshing.dismiss();
//                }
//            }
//        };
//        registerReceiver(mWifiScanReceiver,
//                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//        wifiRefreshing.show();
    }

    private void onError(String msg) {
        new AlertDialog.Builder(BluetoothConfigActivity.this)
                .setTitle("Error")
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private String waitForResponse(InputStream mmInputStream, long timeout) throws IOException {
        int bytesAvailable;

        while (true) {
            bytesAvailable = mmInputStream.available();
            if (bytesAvailable > 0) {
                byte[] packetBytes = new byte[bytesAvailable];
                byte[] readBuffer = new byte[1024];
                mmInputStream.read(packetBytes);

                for (int i = 0; i < bytesAvailable; i++) {
                    byte b = packetBytes[i];

                    if (b == delimiter) {
                        byte[] encodedBytes = new byte[readBufferPosition];
                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                        final String data = new String(encodedBytes, "US-ASCII");
                        int index = data.lastIndexOf("ip-addres");
                        if (index > 0)
                            return data.substring(index+10);
                        return null;
                    } else {
                        readBuffer[readBufferPosition++] = b;
                    }
                }
            }else {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException ignored) {}
            }
        }
    }


    // **************************** CLASS ****************************** //
    @SuppressLint("StaticFieldLeak")
    final class WorkerThread extends AsyncTask<Void,Void,String> {
        private String ssid;
        private String psk;
        private BluetoothDevice device;

        private String errorMessage;

        WorkerThread(String ssid, String psk, BluetoothDevice device) {
            this.ssid = ssid;
            this.psk = psk;
            this.device = device;
            errorMessage = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            settingUpConnection.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                mmSocket = device.createRfcommSocketToServiceRecord(uuid);
                if (!mmSocket.isConnected()) {
                    mmSocket.connect();
                    Thread.sleep(1000);
                }

                OutputStream mmOutputStream = mmSocket.getOutputStream();
                final InputStream mmInputStream = mmSocket.getInputStream();
                String ip = waitForResponse(mmInputStream, 101);
                if( ip != null )
                    return ip;

                mmOutputStream.write(ssid.getBytes());
                mmOutputStream.flush();
                ip = waitForResponse(mmInputStream, 100);
                if( ip != null )
                    return ip;

                mmOutputStream.write(psk.getBytes());
                mmOutputStream.flush();
                ip = waitForResponse(mmInputStream, 100);
                if( ip != null )
                    return ip;
                mmSocket.close();
            } catch (Exception e) {
                errorMessage = "Error: " + e.getLocalizedMessage();
                return null;
            }
            errorMessage = "Error: Didn't get the IP information from device";
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            settingUpConnection.dismiss();
            startButton.setEnabled(true);
            if(s == null)
                onError(errorMessage);
            else
                new ConnectToDevice(s).execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    final class ConnectToDevice extends AsyncTask<Void,Void,Integer> {
        private String ip;
        private String errMsg;
        ConnectToDevice(String ip){
            this.ip = ip;
            errMsg = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            connectingViaWifi.show();
        }

        @Override
        protected Integer doInBackground(Void... strings) {
            try {
                new SSHInterface("pi","nehremislove13",ip);
            } catch (JSchException e) {
                errMsg = e.getLocalizedMessage();
                return 1;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            connectingViaWifi.dismiss();
            if (integer == 0){
                Toast.makeText(BluetoothConfigActivity.this,
                        "Successfully set up the camera! Go to settings and choose to use this camera.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            else {
                onError(errMsg);
            }
        }
    }
}