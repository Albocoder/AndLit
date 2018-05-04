package com.andlit.ui.knownPeopleView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.andlit.R;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.KnownPPL;
import com.andlit.database.entities.misc_info;
import java.util.List;

public class PersonInfoRVAdapter extends RecyclerView.Adapter<PersonInfoRVAdapter.PersonInfoViewHolder>
{
    private String personId;
    private List<misc_info> miscInfoList;
    private int fixedInfoSize = 4;
    private FragmentManager supportFragmentManager;

    PersonInfoRVAdapter(String personId, List<misc_info> miscInfoList, FragmentManager supportFragmentManager)
    {
        super();

        this.personId = personId;
        this.miscInfoList = miscInfoList;
        this.supportFragmentManager = supportFragmentManager;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // provide access to all the views for a data item in a view holder
    static class PersonInfoViewHolder extends RecyclerView.ViewHolder
    {
        CardView cv;
        TextView personInfoDesc;
        TextView personInfoItem;
        ImageButton removeButton;

        PersonInfoViewHolder(View itemView)
        {
            super(itemView);
            cv = itemView.findViewById(R.id.cvPersonInfoItem);
            personInfoDesc = itemView.findViewById(R.id.person_info_desc);
            personInfoItem = itemView.findViewById(R.id.person_info_item);
            removeButton = itemView.findViewById(R.id.remove_info);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PersonInfoRVAdapter.PersonInfoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.person_info_item, viewGroup, false);
        return new PersonInfoViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final PersonInfoRVAdapter.PersonInfoViewHolder viewHolder, int pos)
    {
        final int position = pos;
        Context context = viewHolder.itemView.getContext();

        final AppDatabase db = AppDatabase.getDatabase(context);

        KnownPPL person = db.knownPplDao().getPersonWithID(Integer.parseInt(personId));

        String editInfoDesc;
        String editInfoItem;

        boolean fixedItem = true;

        switch( position )
        {
            case 0:
                viewHolder.personInfoDesc.setText("Name: ");
                viewHolder.personInfoItem.setText(person.name);
                editInfoDesc = "Name: ";
                editInfoItem = person.name;
                break;
            case 1:
                viewHolder.personInfoDesc.setText("Surname: ");
                viewHolder.personInfoItem.setText(person.sname);
                editInfoDesc = "Surname: ";
                editInfoItem = person.sname;
                break;
            case 2:
                viewHolder.personInfoDesc.setText("Age: ");
                viewHolder.personInfoItem.setText("" + person.age);
                editInfoDesc = "Age: ";
                editInfoItem = "" + person.age;
                break;
            case 3:
                viewHolder.personInfoDesc.setText("Address: ");
                viewHolder.personInfoItem.setText(person.address);
                editInfoDesc = "Address: ";
                editInfoItem = person.address;
                break;
            default:
                viewHolder.personInfoDesc.setText(miscInfoList.get(position-fixedInfoSize).key);
                viewHolder.personInfoItem.setText(miscInfoList.get(position-fixedInfoSize).desc);
                editInfoDesc = miscInfoList.get(position-fixedInfoSize).key;
                editInfoItem = miscInfoList.get(position-fixedInfoSize).desc;
                fixedItem = false;
                break;
        }

        if( position > fixedInfoSize-1 )
        {
            Drawable myDrawable = viewHolder.itemView.getResources().getDrawable(R.drawable.ic_delete_forever_white);
            viewHolder.removeButton.setImageDrawable(myDrawable);

            viewHolder.removeButton.setVisibility(View.VISIBLE);

            // Set a click listener for item remove button
            viewHolder.removeButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    // remove from db
                    db.miscInfoDao().deleteEntry(miscInfoList.get(position-fixedInfoSize));

                    // Remove from view
                    miscInfoList.remove(position-fixedInfoSize);
                    notifyItemRemoved(position-fixedInfoSize);
                    notifyItemRangeChanged(position-fixedInfoSize, miscInfoList.size()+fixedInfoSize);
                }
            });
        }
        else
        {
            viewHolder.removeButton.setVisibility(View.INVISIBLE);
        }

        final String desc = editInfoDesc;
        final String item = editInfoItem;
        final boolean finalFixedItem = fixedItem;
        viewHolder.cv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // edit info dialogue
                EditMiscInfoDialogFragment editMiscInfoDialogFragment = new EditMiscInfoDialogFragment();

                Bundle bundle = new Bundle();
                bundle.putString("PERSON_ID", personId);
                bundle.putString("DESC", desc);
                bundle.putString("ITEM", item);
                bundle.putBoolean("FIXED_ITEM", finalFixedItem);

                editMiscInfoDialogFragment.setArguments(bundle);

                editMiscInfoDialogFragment.show(supportFragmentManager, "EDIT_INFO_DIALOG");
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        if( miscInfoList != null )
            return miscInfoList.size() + fixedInfoSize;   // return the number of cards you want
        return 5;
    }
}
