package com.andlit.ui.knownPeopleView;

import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import com.andlit.R;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.misc_info;
import java.util.List;

public class PersonInfoActivity extends AppCompatActivity implements DialogCloseListener
{
    private RecyclerView rv;
    private String personId;
    private List<misc_info> miscInfoList;
    PersonInfoRVAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_person_info);

        rv = findViewById(R.id.rvPersonInfo);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        personId = getIntent().getStringExtra("PERSON_ID");

        initializeData();
        initializeAdapter();

        final AddMiscInfoDialogFragment addMiscInfoDialogFragment = new AddMiscInfoDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putString("PERSON_ID", personId);
        addMiscInfoDialogFragment.setArguments(bundle);

        final Button addMiscInfoButton = findViewById(R.id.button_add_info);
        addMiscInfoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                addMiscInfoDialogFragment.show(getSupportFragmentManager(), "ADD_INFO_DIALOG");
            }
        });
    }

    private void initializeData()
    {
        AppDatabase db = AppDatabase.getDatabase(this);
        miscInfoList = db.miscInfoDao().getInfosForID(Integer.parseInt(personId));
    }

    private void initializeAdapter()
    {
        adapter = new PersonInfoRVAdapter(personId, miscInfoList, getSupportFragmentManager());
        rv.setAdapter(adapter);
    }

    @Override
    public void handleDialogClose(DialogInterface dialog)
    {
        rv.invalidate();
        initializeData();
        initializeAdapter();
    }
}
