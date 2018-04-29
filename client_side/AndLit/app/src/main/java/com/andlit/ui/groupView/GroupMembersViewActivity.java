package com.andlit.ui.groupView;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.andlit.R;
import java.util.ArrayList;
import java.util.List;

public class GroupMembersViewActivity extends AppCompatActivity
{
    private RecyclerView rv;
    private List groups;
    private String groupName;
    private boolean admin;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_members_view);

        admin = getIntent().getBooleanExtra("ADMIN", false);
        groupName = getIntent().getStringExtra("groupName");
        
        Button button = findViewById(R.id.query_all);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 4/26/18 Query Entire Group 
            }
        });

        rv = findViewById(R.id.rvGroupMembersView);

        LinearLayoutManager llm = new LinearLayoutManager(this.getApplicationContext());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeData();
        initializeAdapter();
    }

    private void initializeData()
    {
        // TODO: 4/25/18 initialize using actual data

        groups = new ArrayList();
        groups.add(new Group("Hamza", true));
        groups.add(new Group("Khan", false));
    }

    private void initializeAdapter()
    {
        GroupMembersViewRVAdapter adapter = new GroupMembersViewRVAdapter(groups, admin);
        rv.setAdapter(adapter);
    }
}
