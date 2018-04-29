package com.andlit.ui.groupView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.andlit.R;
import java.util.ArrayList;
import java.util.List;

public class YourGroupsFragment extends Fragment
{
    private RecyclerView rv;
    private List groups;

    public YourGroupsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_your_groups, container, false);

        rv = view.findViewById(R.id.rvGroupsView);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeData();
        initializeAdapter();

        return view;
    }

    private void initializeData()
    {
        // TODO: 4/25/18 initialize using actual data


        groups = new ArrayList();
        groups.add(new Group("Football", true));
        groups.add(new Group("Hockey", false));
    }

    private void initializeAdapter()
    {
        GroupViewRVAdapter adapter = new GroupViewRVAdapter(groups);
        rv.setAdapter(adapter);
    }
}
