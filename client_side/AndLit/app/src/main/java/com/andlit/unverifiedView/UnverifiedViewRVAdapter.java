package com.andlit.unverifiedView;

import android.view.View;
import com.andlit.trainingView.Person;
import com.andlit.trainingView.TrainingViewRVAdapter;
import java.util.List;

public class UnverifiedViewRVAdapter extends TrainingViewRVAdapter
{
    UnverifiedViewRVAdapter(List<Person> persons)
    {
        super(persons);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, final int position)
    {
        personViewHolder.personName.setText(persons.get(position).name);
        personViewHolder.personAge.setText(persons.get(position).age);
        personViewHolder.personPhoto.setImageResource(persons.get(position).photoId);

        // Set a click listener for item remove button
        personViewHolder.removeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Remove the item on remove button click
                persons.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,persons.size());

                // To Do: Remove from database as well

            }
        });

        personViewHolder.personPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // To Do: dialog appearance

            }
        });
    }
}
