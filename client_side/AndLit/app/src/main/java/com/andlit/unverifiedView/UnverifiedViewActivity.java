package com.andlit.unverifiedView;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.andlit.R;
import com.andlit.trainingView.Person;
import com.andlit.trainingView.TrainingViewRVAdapter;
import java.util.ArrayList;
import java.util.List;

public class UnverifiedViewActivity extends Activity
{
    private List<Person> persons;
    private RecyclerView rv;

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
        // To Do: initialize persons using actual data

        persons = new ArrayList<>();
        persons.add(new Person("Hamza Wilson", "23 years old", R.drawable.default_profile));
        persons.add(new Person("Lavery Maiss", "25 years old", R.drawable.default_profile));
        persons.add(new Person("Lillie Watts", "35 years old", R.drawable.default_profile));
    }

    private void initializeAdapter()
    {
        TrainingViewRVAdapter adapter = new UnverifiedViewRVAdapter(persons);
        rv.setAdapter(adapter);
    }
}
