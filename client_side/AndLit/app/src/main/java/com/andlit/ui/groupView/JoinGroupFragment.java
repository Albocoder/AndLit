package com.andlit.ui.groupView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.andlit.R;
import com.andlit.cloudInterface.pools.PoolOps;
import com.andlit.database.entities.Pool;

public class JoinGroupFragment extends Fragment
{
    Context context;
    TextInputLayout usernameView;
    TextInputLayout passwordView;

    public JoinGroupFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_join_group, container, false);

        usernameView = view.findViewById(R.id.join_group_id);
        passwordView = view.findViewById(R.id.join_group_pass);

        context = getContext();

        Button button = view.findViewById(R.id.button_join_group);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                // what happens when you press join group button
                String username = usernameView.getEditText().getText().toString();
                String pass = passwordView.getEditText().getText().toString();
                new JoinPoolTask().execute(username, pass);
            }
        });

        return view;
    }

    @SuppressLint("StaticFieldLeak")
    private class JoinPoolTask extends AsyncTask<String, Void, Integer>
    {
        private ProgressDialog progressDialog = new ProgressDialog(context, R.style.AppTheme_Dark_Dialog);

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
                PoolOps pops = new PoolOps(context);
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
                usernameView.getEditText().getText().clear();
                passwordView.getEditText().getText().clear();
                Toast toast = Toast.makeText(context, "Successfully Joined Pool!", Toast.LENGTH_SHORT);
                toast.show();
            }
            else if(ret == 2)
            {
                Toast toast = Toast.makeText(context, "Couldn't Join Pool!", Toast.LENGTH_SHORT);
                toast.show();
            }
            progressDialog.dismiss();
        }
    }

}
