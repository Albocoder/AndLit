package com.andlit.groupView;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

public class GroupViewRVAdapter extends RecyclerView.Adapter<com.andlit.groupView.GroupViewRVAdapter.GroupViewHolder>
{
    public List<Group> groups;

    public GroupViewRVAdapter(List groups)
    {
        this.groups = groups;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // provide access to all the views for a data item in a view holder
    public static class GroupViewHolder extends RecyclerView.ViewHolder
    {
        CardView cv;
        public TextView groupName;
        public ImageView adminPhoto;
        public ImageButton removeButton;

        GroupViewHolder(View itemView)
        {
            super(itemView);
            cv = itemView.findViewById(R.id.cvGroups);
            groupName = itemView.findViewById(R.id.group_name);
            adminPhoto = itemView.findViewById(R.id.admin_photo);
            removeButton = itemView.findViewById(R.id.remove_group);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public com.andlit.groupView.GroupViewRVAdapter.GroupViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.your_groups_list_item, viewGroup, false);
        com.andlit.groupView.GroupViewRVAdapter.GroupViewHolder pvh = new com.andlit.groupView.GroupViewRVAdapter.GroupViewHolder(v);
        return pvh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final com.andlit.groupView.GroupViewRVAdapter.GroupViewHolder personViewHolder, final int position)
    {
        final String groupName = groups.get(position).getName();
        final boolean admin = groups.get(position).admin;

        personViewHolder.groupName.setText(groupName);

        if(admin)
        {
            Drawable myDrawable = personViewHolder.itemView.getResources().getDrawable(R.drawable.ic_star_rate_white);
            personViewHolder.adminPhoto.setImageDrawable(myDrawable);

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

        personViewHolder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Context context = personViewHolder.itemView.getContext();
                Intent intent = new Intent (context, GroupMembersViewActivity.class);
                intent.putExtra("ADMIN", admin);
                intent.putExtra("GROUP", groupName);
                context.startActivity(intent);
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

