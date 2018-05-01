package com.andlit.ui.knownPeopleView;

import android.content.Context;
import android.view.View;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.KnownPPL;
import com.andlit.ui.trainingView.TrainingViewRVAdapter;
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
        final Context context = personViewHolder.itemView.getContext();

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
                AppDatabase db = AppDatabase.getDatabase(context);
                db.knownPplDao().deletePerson(allKnownPpl.get(position));

                // Remove the item on remove button click
                allKnownPpl.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, allKnownPpl.size());
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

