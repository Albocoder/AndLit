package com.andlit.trainingView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.andlit.R;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.KnownPPL;
import com.andlit.database.entities.training_face;
import com.andlit.face.FaceOperator;
import java.io.File;
import java.util.List;

public class TrainingViewRVAdapter extends RecyclerView.Adapter<TrainingViewRVAdapter.PersonViewHolder>
{
    public List<training_face> persons;

    public TrainingViewRVAdapter(List<training_face> persons)
    {
        this.persons = persons;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // provide access to all the views for a data item in a view holder
    public static class PersonViewHolder extends RecyclerView.ViewHolder
    {
        CardView cv;
        public TextView personName;
        public TextView personAge;
        public ImageView personPhoto;
        public ImageButton removeButton;

        PersonViewHolder(View itemView)
        {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            personName = itemView.findViewById(R.id.person_name);
            personAge = itemView.findViewById(R.id.person_age);
            personPhoto = itemView.findViewById(R.id.person_photo);
            removeButton = itemView.findViewById(R.id.remove_person);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.training_view_item, viewGroup, false);
        return new PersonViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, final int position)
    {
        Integer label = persons.get(position).label;
        final Context context = personViewHolder.itemView.getContext();
        final AppDatabase db = AppDatabase.getDatabase(context);
        if(label == -1)
        {
            personViewHolder.personName.setText(R.string.unknown_person);
            personViewHolder.personAge.setText(R.string.not_available);
        }
        else
        {
            db.knownPplDao().getEntryWithID(label);
            KnownPPL person = db.knownPplDao().getEntryWithID(label);
            personViewHolder.personName.setText(String.format("%s %s", person.name, person.sname));
            personViewHolder.personAge.setText(person.age);
        }

        File imgFile = new  File(FaceOperator.getAbsolutePath(context, persons.get(position)));
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
                FaceOperator.deleteTrainingInstance(context, persons.get(position));
                // Remove the item on remove button click
                persons.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, persons.size());
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return persons.size();
    }
}
