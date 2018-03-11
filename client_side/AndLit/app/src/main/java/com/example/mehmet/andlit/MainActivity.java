package com.example.mehmet.andlit;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import com.example.mehmet.andlit.CloudInterface.Authenticator;
import com.example.mehmet.andlit.Settings.SettingsActivity;
import com.example.mehmet.andlit.Settings.SettingsController;
import com.example.mehmet.andlit.Settings.SettingsDefinedKeys;
import com.example.mehmet.andlit.helperUI.HomeFragment;
import com.example.mehmet.andlit.helperUI.SettingsFragment;
import com.example.mehmet.andlit.helperUI.ShowImageFragment;
import com.example.mehmet.andlit.helperUI.UILocalDBHelper;
import com.example.mehmet.andlit.voice.VoiceGenerator;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";

    Bundle userInfo;
    public boolean voiceControlEnabled;
    public boolean soundEnabled = true;

// todo: do this in the login part
//    private class LoginRunner implements Runnable{
//        Context c;
//        public LoginRunner(Context c){
//            this.c = c;
//        }
//        @Override
//        public void run() {
//            Authenticator a = new Authenticator(c);
//            try {
//                a.logout();
//            } catch (Exception e) {}
//        }
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // todo: do this in login part
//        Thread t = new Thread(new LoginRunner(this));
//        t.start();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        loadSettings();

        HomeFragment tpf = new HomeFragment();
        tpf.setHomeActivity(this);
        switchFragments(R.id.content_frame, tpf);

        checkTTS(); // checks tts installation
        // Settings
        SettingsController.loadSettings(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /*
    method to check if a TTS engine is installed on the device.
    The check is performed by making use of the result of another Activity.
    Only need to run once on each device
    */
    private void checkTTS()
    {
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, 0x1);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        int contentFrameID = R.id.content_frame;
        FragmentManager fragmentManager = getFragmentManager();

        if (id == R.id.nav_home) {
            HomeFragment tpf = new HomeFragment();
            tpf.setHomeActivity(this);
            switchFragments(contentFrameID, tpf);
        } else if (id == R.id.nav_search) {

        } else if (id == R.id.nav_groups) {
            ShowImageFragment sim = new ShowImageFragment();
            switchFragments(contentFrameID, sim);
        } else if (id == R.id.nav_settings) {
//            SettingsFragment sf = new SettingsFragment();
//            switchFragments(contentFrameID, sf);
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_log_out){
            saveSettings();
            UILocalDBHelper uldb = new UILocalDBHelper(this);
            uldb.setAskForPassword(true);
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void switchFragments(int id, Fragment toChange){
        Bundle args = null;
        if(toChange.getClass().getSimpleName().equals("SettingsFragment")){
            args = new Bundle();
            args.putBoolean("voiceControl", voiceControlEnabled);
            args.putBoolean("soundControl", soundEnabled);
        }
        Log.d("snd", "switchFragments: " + soundEnabled);
        toChange.setArguments(args);
        //getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(id)).commit();
        getFragmentManager().beginTransaction().replace(id, toChange).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data); 

        if(requestCode == 0x1)
        {
            if(resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
            {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }
    }

    public void loadSettings(){}
    public void saveSettings(){}
}
