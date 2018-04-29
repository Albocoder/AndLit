package com.andlit.ui.groupView;

import android.graphics.drawable.Drawable;
import android.view.View;
import com.andlit.R;
import java.util.List;

public class GroupMembersViewRVAdapter extends GroupViewRVAdapter
{
    List<Group> groups;
    boolean admin;

    public GroupMembersViewRVAdapter(List persons, boolean admin)
    {
        super(null);

        this.groups = persons;
        this.admin = admin;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final com.andlit.ui.groupView.GroupViewRVAdapter.GroupViewHolder personViewHolder, final int position)
    {
        personViewHolder.groupName.setText(groups.get(position).getName());

        Drawable myDrawable = personViewHolder.itemView.getResources().getDrawable(R.drawable.ic_tag_faces_white);
        personViewHolder.adminPhoto.setImageDrawable(myDrawable);

        if(admin)
        {
            myDrawable = personViewHolder.itemView.getResources().getDrawable(R.drawable.ic_delete_forever_white);
            personViewHolder.removeButton.setImageDrawable(myDrawable);

            // Set a click listener for item remove button
            personViewHolder.removeButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    // TODO: 4/25/18 Remove from database as well


                    // Remove the item on remove button click
                    groups.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, groups.size());
                }
            });
        }
        
        personViewHolder.adminPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 4/26/18 Query this person's classifier
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return groups.size();
    }
}
