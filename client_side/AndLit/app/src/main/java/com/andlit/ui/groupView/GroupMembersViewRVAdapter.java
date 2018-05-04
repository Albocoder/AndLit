package com.andlit.ui.groupView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;
import com.andlit.R;
import com.andlit.cloudInterface.pools.PoolOps;
import com.andlit.cloudInterface.pools.models.PoolMember;
import com.andlit.ui.unverifiedView.UnverifiedViewActivity;
import java.util.List;

public class GroupMembersViewRVAdapter extends GroupViewRVAdapter
{
    private List<PoolMember> members;
    private boolean admin;
    private String poolName;
    private String poolId;
    private Context context;

    GroupMembersViewRVAdapter(List<PoolMember> persons, boolean admin, String poolName, String poolId)
    {
        super(null);

        this.poolId = poolId;
        this.poolName = poolName;
        this.members = persons;
        this.admin = admin;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final com.andlit.ui.groupView.GroupViewRVAdapter.GroupViewHolder personViewHolder, final int position)
    {
        personViewHolder.groupName.setText(members.get(position).getUsername());

        Drawable myDrawable = personViewHolder.itemView.getResources().getDrawable(R.drawable.ic_tag_faces_white);
        personViewHolder.adminPhoto.setImageDrawable(myDrawable);

        this.context = personViewHolder.itemView.getContext();

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
                    // remove from db
                    new kickMemberTask().execute(members.get(position).getUsername());

                    // Remove the item from the view
                    members.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, members.size());
                }
            });
        }
        
        personViewHolder.adminPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Query this person's classifier
                Intent intent = new Intent (context, UnverifiedViewActivity.class);
                intent.putExtra("POOL_QUERY", true);
                intent.putExtra("POOL_ID", poolId);
                long memberId = members.get(position).getId();
                intent.putExtra("MEMBER_ID", memberId);
                context.startActivity(intent);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return members.size();
    }

    @SuppressLint("StaticFieldLeak")
    private class kickMemberTask extends AsyncTask<String, Void, Integer> {
        private ProgressDialog progressDialog = new ProgressDialog(context, R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute() {
            // Display the loading spinner
            progressDialog.setMessage("Removing user...");
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
                pops.kickFromPool(poolId, paramsObj[0]);
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
                Toast toast = Toast.makeText(context, "User Successfully Removed!", Toast.LENGTH_SHORT);
                toast.show();
            }
            else if(ret == 2)
            {
                Toast toast = Toast.makeText(context, "Couldn't Remove User!", Toast.LENGTH_SHORT);
                toast.show();
            }
            progressDialog.dismiss();
        }
    }
}
