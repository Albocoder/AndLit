package com.andlit.ui.groupView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.andlit.R;
import com.andlit.cloudInterface.pools.PoolOps;
import com.andlit.cloudInterface.pools.models.PoolMember;
import java.util.List;

public class GroupMembersViewActivity extends AppCompatActivity
{
    private RecyclerView rv;
    private List<PoolMember> members;
    private boolean admin;
    private String groupId;
    private Context context;
    private String groupPass;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_members_view);

        admin = getIntent().getBooleanExtra("ADMIN", false);
        groupId = getIntent().getStringExtra("POOL_ID");
        groupPass = getIntent().getStringExtra("POOL_PASS");

        context = this;
        
        Button button = findViewById(R.id.pool_info_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // give pool info to the user
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String poolData = "ID: " + groupId + "\n" + "Pass: " + groupPass;
                ClipData clip = ClipData.newPlainText("POOL_INFO", poolData);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                }
                Toast toast = Toast.makeText(context, "Pool Info Has Been Copied To Clipboard!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        
        Button button1 = findViewById(R.id.pool_share_button);
        button1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // give pool link to the user
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String poolData = getResources().getString(R.string.pool_join_url) + "?id=" + groupId + "&pw=" + groupPass;
                ClipData clip = ClipData.newPlainText("POOL_INFO", poolData);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                }
                Toast toast = Toast.makeText(context, "Pool Link Has Been Copied To Clipboard!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        rv = findViewById(R.id.rvGroupMembersView);

        LinearLayoutManager llm = new LinearLayoutManager(this.getApplicationContext());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        new getPoolMembersTask().execute();
    }

    private void initializeAdapter()
    {
        GroupMembersViewRVAdapter adapter = new GroupMembersViewRVAdapter(members, admin, groupId);
        rv.setAdapter(adapter);
    }

    @SuppressLint("StaticFieldLeak")
    private class getPoolMembersTask extends AsyncTask<String, Void, Integer>
    {

        private ProgressDialog progressDialog = new ProgressDialog(context, R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute()
        {
            // Display the loading spinner
            progressDialog.setMessage("Loading Members' List...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setInverseBackgroundForced(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... paramsObj)
        {
            try
            {
                PoolOps pops = new PoolOps(context);
                members = pops.getMembersOfPool(groupId);
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
                initializeAdapter();
            }
            else if(ret == 2)
            {
                Toast toast = Toast.makeText(context, "Couldn't Get Pool Members From Server!", Toast.LENGTH_SHORT);
                toast.show();
            }

            progressDialog.dismiss();
        }
    }
}
