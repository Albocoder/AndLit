package com.andlit.trainingView;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.andlit.R;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.training_face;
import java.util.List;

public class TrainingViewActivity extends Activity
{
    private RecyclerView rv;
    private List<training_face> faces;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_training_view);

        rv = findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeData();
        initializeAdapter();
    }

    private void initializeData()
    {
        // initialize persons using actual data
        AppDatabase db = AppDatabase.getDatabase(this);
        faces = db.trainingFaceDao().getAllRecords();
    }

    private void initializeAdapter()
    {
        TrainingViewRVAdapter adapter = new TrainingViewRVAdapter(faces);
        rv.setAdapter(adapter);
    }
}
