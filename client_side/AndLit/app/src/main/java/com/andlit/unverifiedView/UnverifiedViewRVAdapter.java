package com.andlit.unverifiedView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.KnownPPL;
import com.andlit.database.entities.detected_face;
import com.andlit.face.FaceOperator;
import com.andlit.trainingView.TrainingViewRVAdapter;
import java.io.File;
import java.util.List;

public class UnverifiedViewRVAdapter extends TrainingViewRVAdapter
{
    List<detected_face> persons;

    UnverifiedViewRVAdapter(List<detected_face> persons)
    {
        super(null);

        this.persons = persons;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, final int position)
    {
        Integer predictedLabel = persons.get(position).predictedlabel;
        final Context context = personViewHolder.itemView.getContext();
        final AppDatabase db = AppDatabase.getDatabase(context);

        if(predictedLabel == -1)
        {
            personViewHolder.personName.setText("Unknown Person");
            personViewHolder.personAge.setText("Age Not Available");
        }
        else
        {
            db.knownPplDao().getEntryWithID(predictedLabel);
            KnownPPL person = db.knownPplDao().getEntryWithID(predictedLabel);
            personViewHolder.personName.setText(person.name + " " + person.sname);
            personViewHolder.personAge.setText(person.age + " years old");
        }


        File imgFile = new File(FaceOperator.getAbsolutePath(context, persons.get(position)));
        if(imgFile.exists())
        {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            personViewHolder.personPhoto.setImageBitmap(myBitmap);
        }

        // Set a click listener for item remove button
        personViewHolder.removeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Remove from database as well
                FaceOperator.deleteDetectionInstance(context, persons.get(position));

                // Remove the item on remove button click
                persons.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,persons.size());
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

    @Override
    public int getItemCount()
    {
        return persons.size();
    }
}
