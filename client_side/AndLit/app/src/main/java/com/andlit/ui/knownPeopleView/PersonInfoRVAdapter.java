package com.andlit.ui.knownPeopleView;

import android.content.Context;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.KnownPPL;
import com.andlit.database.entities.misc_info;
import com.andlit.ui.classifierView.ClassifierViewRVAdapter;

import java.util.List;

public class PersonInfoRVAdapter extends ClassifierViewRVAdapter
{
    String personId;
    List<misc_info> miscInfoList;

    public PersonInfoRVAdapter(String personId)
    {
        super();

        this.personId = personId;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final com.andlit.ui.classifierView.ClassifierViewRVAdapter.ClassifierViewHolder viewHolder, final int position)
    {
        Context context = viewHolder.itemView.getContext();

        AppDatabase db = AppDatabase.getDatabase(context);

        KnownPPL person = db.knownPplDao().getPersonWithID(Integer.parseInt(personId));
        db.miscInfoDao().getInfosForID(Integer.parseInt(personId));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return 4;   // return the number of cards you want
    }
}
