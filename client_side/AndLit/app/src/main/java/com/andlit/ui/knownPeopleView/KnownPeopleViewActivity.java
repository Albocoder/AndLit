package com.andlit.ui.knownPeopleView;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.andlit.R;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.KnownPPL;
import java.util.List;

public class KnownPeopleViewActivity extends AppCompatActivity
{
    private RecyclerView rv;
    private List<KnownPPL> kp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_known_people_view);

        rv = findViewById(R.id.rvKnownPeople);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeData();
        initializeAdapter();
    }

    private void initializeData()
    {
        AppDatabase db = AppDatabase.getDatabase(this);

        kp = db.knownPplDao().getAllRecords();
    }

    private void initializeAdapter()
    {
        KnownPeopleViewRVAdapter adapter = new KnownPeopleViewRVAdapter(kp);
        rv.setAdapter(adapter);
    }
}
