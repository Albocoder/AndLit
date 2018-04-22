package com.andlit;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.andlit.cloudInterface.authentication.Authenticator;
import com.andlit.cron.CronMaster;
import com.andlit.face.FaceRecognizerSingleton;
import com.andlit.settings.SettingsActivity;
import com.andlit.ui.HandsFreeMode;
import com.andlit.ui.IntermediateCameraActivity;


public class HomeActivity extends AppCompatActivity
{
    private static final String TAG = "HomeActivity";

    // View related Properties
    private DrawerLayout mDrawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        navigationDrawerInit();

        checkTTS();

        // Camera button init
        Button cameraButton = findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                loadCameraScreen();
            }
        });

        Button handsFree = findViewById(R.id.handsfree_button);
        handsFree.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                loadHandsFreeMode();
            }
        });

        Button trainingButton = findViewById(R.id.training_button);
        trainingButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                new FaceRecognizerSingleton(view.getContext()).trainModel();
            }
        });

        // *********************** perform all the on-start operations *********************** //

        CronMaster.fireAllCrons(this);
    }

    public void navigationDrawerInit()
    {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        int id = menuItem.getItemId();
                        Context context = navigationView.getContext();

                        if (id == R.id.nav_logout)
                        {
                            // handle logout and try to backup if not than no prob
                            new AlertDialog.Builder(HomeActivity.this)
                                .setTitle("Do you really want to logout?")
                                .setMessage(Html.fromHtml("Logging out will delete all your data. " +
                                        "When logging in you have to wait for all to come back. <br>" +
                                        "<b>Tip:</b> Use lock to protect your data."))
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        new LogoutTask().execute();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).show();
                        }
                        else if (id == R.id.nav_settings)
                        {
                            // Handle Settings
                            loadSettingsScreen();
                        }
                        else if (id == R.id.nav_lock)
                        {
                            // Handle Lock
                            if(new Authenticator(context).lock())
                                goToLoginScreen();
                            else
                                Log.d(TAG,"Locking didn't work for some reason");
                        }

                        return true;
                    }
                });
    }

    // NavBar button init
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadHandsFreeMode(){
        Intent i = new Intent(this, HandsFreeMode.class);
        startActivity(i);
    }

    private void loadSettingsScreen()
    {
        Intent intent = new Intent(this.getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    private void loadCameraScreen(){
        Intent i = new Intent(this, IntermediateCameraActivity.class);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Audio Feedback
        if(requestCode == RequestCodes.AUDIO_FEEDBACK_RC)
        {
            if(resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
            {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }
    }

    /*
    method to check if a TTS engine is installed on the device.
    The check is performed by making use of the result of another Activity.
    */
    private void checkTTS()
    {
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, RequestCodes.AUDIO_FEEDBACK_RC);
    }

    private void goToLoginScreen(){
        Intent i = new Intent(this,LoginActivity.class);
        startActivity(i);
        finish();
    }


    // ************************ ASYNC TASKS ****************************//
    @SuppressLint("StaticFieldLeak")
    private class LogoutTask extends AsyncTask<String, Void, Integer> {

        private ProgressDialog progressDialog =
                new ProgressDialog(HomeActivity.this,R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute() {
            // Display the loading spinner
            progressDialog.setTitle("Logging out...");
            progressDialog.setMessage("Please wait while we delete all the data.");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setInverseBackgroundForced(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... paramsObj) {
            new Authenticator(HomeActivity.this).logout();
            return 0;
        }

        @Override
        protected void onPostExecute(Integer ret) {
            progressDialog.dismiss();
            goToLoginScreen();
        }
    }
}
