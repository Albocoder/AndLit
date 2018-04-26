package com.andlit.unverifiedView;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.andlit.R;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.KnownPPL;
import com.andlit.database.entities.detected_face;
import com.andlit.trainingView.TrainingViewRVAdapter;

import java.util.List;

public class UnverifiedViewActivity extends Activity
{
    private RecyclerView rv;
    private List<detected_face> faces;
    private List<KnownPPL> kp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_unverified_view);

        rv = findViewById(R.id.rvUnverified);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeData();
        initializeAdapter();
    }

    private void initializeData()
    {
        AppDatabase db = AppDatabase.getDatabase(this);
        faces = db.detectedFacesDao().getAllRecords();
        kp = db.knownPplDao().getAllRecords();
    }

    private void initializeAdapter()
    {
        TrainingViewRVAdapter adapter = new UnverifiedViewRVAdapter(faces,kp);
        rv.setAdapter(adapter);
    }
}
