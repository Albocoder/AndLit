package com.andlit.knownPeopleView;

import android.view.View;
import com.andlit.database.entities.KnownPPL;
import com.andlit.trainingView.TrainingViewRVAdapter;
import java.util.List;

public class KnownPeopleViewRVAdapter extends TrainingViewRVAdapter
{
    List<KnownPPL> allKnownPpl;

    KnownPeopleViewRVAdapter( List<KnownPPL> known )
    {
        super(null);

        allKnownPpl = known;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, final int position)
    {
        KnownPPL person = allKnownPpl.get(position);
        personViewHolder.personName.setText(person.name + " " + person.sname);
        personViewHolder.personAge.setText(person.age + " years old");

        // Set a click listener for item remove button
        personViewHolder.removeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Remove from database as well


                // Remove the item on remove button click
                persons.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, persons.size());
            }
        });

        // set a click listener for the card view
        personViewHolder.cv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // TODO: 4/29/18 pop up appearance for edit info
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return allKnownPpl.size();
    }
}

