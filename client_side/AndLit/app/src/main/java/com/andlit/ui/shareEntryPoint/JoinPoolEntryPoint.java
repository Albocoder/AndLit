package com.andlit.ui.shareEntryPoint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.andlit.R;
import com.andlit.cloudInterface.pools.PoolOps;
import com.andlit.database.entities.Pool;

public class JoinPoolEntryPoint extends Activity {
    private static final String TAG = "JoinPoolEntryPoint";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String data = intent.getDataString();
        int idIndex = data.indexOf("?id=");
        int idEndIndex = data.indexOf("&pw");
        if(idIndex == -1 || idEndIndex == -1){
            Toast.makeText(this,R.string.invalid_url,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        final String id = data.substring(idIndex+4,idEndIndex);
        final String pw = data.substring(idEndIndex+4);
        if(pw.length() != 10 && id.length() != 36) {
            Toast.makeText(this,R.string.invalid_url,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Join pool?")
                .setMessage("Do you want to join this pool?")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new JoinPoolTask().execute(id,pw);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }
    @SuppressLint("StaticFieldLeak")
    private class JoinPoolTask extends AsyncTask<String, Void, Integer>
    {
        private ProgressDialog progressDialog = new ProgressDialog(JoinPoolEntryPoint.this, R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute() {
            // Display the loading spinner
            progressDialog.setMessage("Joining Pool...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setInverseBackgroundForced(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... paramsObj)
        {
            try
            {
                PoolOps pops = new PoolOps(JoinPoolEntryPoint.this);
                Pool pool = pops.joinPool(paramsObj[0], paramsObj[1]);

                if(pool == null)
                    return 2;
            }
            catch( Exception e )
            {
                return 2;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer ret)
        {
            if(ret == 0)
            {
                Toast toast = Toast.makeText(JoinPoolEntryPoint.this, "Successfully Joined Pool!", Toast.LENGTH_SHORT);
                toast.show();
            }
            else if(ret == 2)
            {
                Toast toast = Toast.makeText(JoinPoolEntryPoint.this, "Couldn't Join Pool, no internet or wrong credentials!", Toast.LENGTH_SHORT);
                toast.show();
            }
            progressDialog.dismiss();
            finish();
        }
    }
}
