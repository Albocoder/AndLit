package com.andlit.trainingView;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.andlit.R;
import java.util.List;

public class TrainingViewRVAdapter extends RecyclerView.Adapter<TrainingViewRVAdapter.PersonViewHolder>
{
    public List<Person> persons;

    public TrainingViewRVAdapter(List<Person> persons)
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
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
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
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return persons.size();
    }
}
