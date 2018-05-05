package com.andlit.ui.groupView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.andlit.R;
import com.andlit.cloudInterface.pools.PoolOps;
import com.andlit.database.entities.Pool;
import java.util.List;

public class GroupViewRVAdapter extends RecyclerView.Adapter<com.andlit.ui.groupView.GroupViewRVAdapter.GroupViewHolder>
{
    private List<Pool> groups;
    private Context context;

    GroupViewRVAdapter(List<Pool> groups)
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
    public com.andlit.ui.groupView.GroupViewRVAdapter.GroupViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.your_groups_list_item, viewGroup, false);
        return new GroupViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final com.andlit.ui.groupView.GroupViewRVAdapter.GroupViewHolder personViewHolder, int pos)
    {
        final int position = pos;

        context = personViewHolder.itemView.getContext();

        final String groupName = groups.get(position).name;
        final boolean admin = groups.get(position).is_creator;

        personViewHolder.groupName.setText(groupName);

        if(admin)
        {
            Drawable myDrawable = personViewHolder.itemView.getResources().getDrawable(R.drawable.ic_star_rate_white);
            personViewHolder.adminPhoto.setImageDrawable(myDrawable);
        }

        Drawable myDrawable = personViewHolder.itemView.getResources().getDrawable(R.drawable.ic_delete_forever_white);
        personViewHolder.removeButton.setImageDrawable(myDrawable);

        // Set a click listener for item remove button
        personViewHolder.removeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // remove from db
                new LeavePoolTask().execute(groups.get(position).id);

                // Remove from view
                groups.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, groups.size());
            }
        });

        personViewHolder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Context context = personViewHolder.itemView.getContext();
                Intent intent = new Intent (context, GroupMembersViewActivity.class);
                intent.putExtra("ADMIN", admin);
                intent.putExtra("POOL_ID", groups.get(position).id);
                intent.putExtra("POOL_PASS", groups.get(position).password);
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

    @SuppressLint("StaticFieldLeak")
    private class LeavePoolTask extends AsyncTask<String, Void, Integer>
    {
        private ProgressDialog progressDialog = new ProgressDialog(context, R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute() {
            // Display the loading spinner
            progressDialog.setMessage("Leaving Pool...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setInverseBackgroundForced(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... paramsObj)
        {
            try
            {
                PoolOps pops = new PoolOps(context);
                pops.leavePool(paramsObj[0]);
            }
            catch( Exception e )
            {
                return 2;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer ret)
        {
            if(ret == 0)
            {
                Toast toast = Toast.makeText(context, "Pool Successfully Left!", Toast.LENGTH_SHORT);
                toast.show();
            }
            else if(ret == 2)
            {
                Toast toast = Toast.makeText(context, "Couldn't Leave Pool!", Toast.LENGTH_SHORT);
                toast.show();
            }
            progressDialog.dismiss();
        }
    }
}

