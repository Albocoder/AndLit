package com.andlit.ui.unverifiedView;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.andlit.R;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.KnownPPL;
import com.andlit.database.entities.detected_face;
import com.andlit.ui.trainingView.TrainingViewRVAdapter;
import java.util.List;

public class UnverifiedViewActivity extends Activity
{
    private RecyclerView rv;
    private List<detected_face> faces;
    private List<KnownPPL> kp;
    private boolean poolQuery;
    private String poolId;
    private String memberId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_unverified_view);

        rv = findViewById(R.id.rvUnverified);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        poolQuery = getIntent().getBooleanExtra("POOL_QUERY", false);
        poolId = getIntent().getStringExtra("POOL_ID");
        memberId = getIntent().getStringExtra("MEMBER_ID");

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
        TrainingViewRVAdapter adapter = new UnverifiedViewRVAdapter(faces, kp, poolQuery, poolId, memberId);
        rv.setAdapter(adapter);
    }
}
