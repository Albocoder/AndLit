package com.andlit.ui.groupView;

import android.annotation.SuppressLint;
import android.app.Activity;
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

public class CreateGroupFragment extends Fragment
{
    TextInputLayout poolNameInput;
    Context context;
    Activity activity;

    public CreateGroupFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_create_group, container, false);

        poolNameInput = view.findViewById(R.id.create_group_name);

        context = view.getContext();
        activity = getActivity();

        Button button = view.findViewById(R.id.button_create_group);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                // what happens when you press create group button
                new CreatePoolTask().execute(poolNameInput.getEditText().getText().toString());
            }
        });

        return view;
    }

    @SuppressLint("StaticFieldLeak")
    private class CreatePoolTask extends AsyncTask<String, Void, Integer> {

        private ProgressDialog progressDialog =
                new ProgressDialog(CreateGroupFragment.this.getContext()
                        ,R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute() {
            // Display the loading spinner
            progressDialog.setMessage("Creating group...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setInverseBackgroundForced(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... paramsObj) {
            if(paramsObj.length <= 0)
                return 1;
            try{
                PoolOps pops = new PoolOps(context);
                pops.createPool(paramsObj[0]);
            }catch (Exception e) {
                return 2;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer ret) {
            if(ret == 0)
            {
                poolNameInput.getEditText().getText().clear();
                Toast toast = Toast.makeText(context, "Successfully Created Pool!", Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                Toast toast = Toast.makeText(context, "Couldn't Create Pool!", Toast.LENGTH_SHORT);
                toast.show();
            }

            progressDialog.dismiss();
        }
    }
}
