package com.andlit.ui.knownPeopleView;

import android.view.View;
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
                persons.remove(position); // TODO: fix this bug here when you click delete
                /*
        java.lang.NullPointerException: Attempt to invoke interface method 'java.lang.Object java.util.List.remove(int)' on a null object reference
        at com.andlit.ui.knownPeopleView.KnownPeopleViewRVAdapter$1.onClick(KnownPeopleViewRVAdapter.java:37)
        at android.view.View.performClick(View.java:5637)
        at android.view.View$PerformClick.run(View.java:22433)
        at android.os.Handler.handleCallback(Handler.java:751)
        at android.os.Handler.dispatchMessage(Handler.java:95)
        at android.os.Looper.loop(Looper.java:179)
        at android.app.ActivityThread.main(ActivityThread.java:6152)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:886)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:776)

                * */
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

