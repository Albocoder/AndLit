package com.andlit.ui.groupView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.andlit.R;
import com.andlit.cloudInterface.pools.PoolOps;
import com.andlit.database.entities.Pool;
import com.andlit.ui.classifierView.ClassifierViewActivity;

import java.util.List;

public class YourGroupsFragment extends Fragment
{
    private RecyclerView rv;
    private List<Pool> pools;
    private TextView text;
    private Context context;

    public YourGroupsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_your_groups, container, false);

        text = view.findViewById(R.id.textView6);
        rv = view.findViewById(R.id.rvGroupsView);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        context = getContext();

        new getPoolsTask().execute();

        return view;
    }

    private void initializeAdapter()
    {
        if(pools != null)
        {
            GroupViewRVAdapter adapter = new GroupViewRVAdapter(pools);
            rv.setAdapter(adapter);
            text.setText("");
        }
        else
        {
            text.setText("You are not a member of any Pool");
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class getPoolsTask extends AsyncTask<String, Void, Integer>
    {
        @Override
        protected Integer doInBackground(String... paramsObj)
        {
            try
            {
                PoolOps pops = new PoolOps(context);
                pools = pops.listPools();
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
                Toast toast = Toast.makeText(context, "Couldn't Get Pools From Server!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
