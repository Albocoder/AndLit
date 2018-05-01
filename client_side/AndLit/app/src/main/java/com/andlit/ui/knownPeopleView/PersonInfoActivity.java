package com.andlit.ui.knownPeopleView;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.andlit.R;

public class PersonInfoActivity extends AppCompatActivity
{
    private RecyclerView rv;
    private String personId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_person_info);

        rv = findViewById(R.id.rvKnownPeople);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        personId = getIntent().getStringExtra("PERSON_ID");

        initializeData();
        initializeAdapter();
    }

    private void initializeData()
    {

    }

    private void initializeAdapter()
    {
        PersonInfoRVAdapter adapter = new PersonInfoRVAdapter(personId);
        rv.setAdapter(adapter);
    }
}
