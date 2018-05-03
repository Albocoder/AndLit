package com.andlit.ui.knownPeopleView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.andlit.R;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.KnownPPL;
import com.andlit.database.entities.misc_info;

public class EditMiscInfoDialogFragment extends DialogFragment
{
    PersonInfoRVAdapter adapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final String desc = getArguments().getString("DESC");
        final String info = getArguments().getString("ITEM");
        final boolean fixedItem = getArguments().getBoolean("FIXED_ITEM", false);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.add_misc_info_dialog, null);
        builder.setView(view);

        final EditText descField = view.findViewById(R.id.desc);
        final EditText infoField = view.findViewById(R.id.info_edit);

        descField.setText(desc, TextView.BufferType.EDITABLE);
        infoField.setText(info, TextView.BufferType.EDITABLE);

        descField.setFocusable(false);

        // Add action buttons
        builder.setPositiveButton("Edit Info", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Edit info
                String personId = getArguments().getString("PERSON_ID");

                String infoNew = infoField.getText().toString();

                AppDatabase db = AppDatabase.getDatabase(getContext());

                if(fixedItem)
                {
                    KnownPPL person = db.knownPplDao().getPersonWithID(Integer.parseInt(personId));
                    person.sname = infoNew;
                    switch( desc )
                    {
                        case "Name: ":
                            person.name = infoNew;
                            break;
                        case "Surname: ":
                            person.sname = infoNew;
                            break;
                        case "Age: ":
                            person.age = Integer.parseInt(infoNew);
                            break;
                        case "Address: ":
                            person.address = infoNew;
                            break;
                    }
                    db.knownPplDao().updatePerson(person);
                }
                else
                {
                    misc_info miscInfo = new misc_info(Integer.parseInt(personId), desc, infoNew);
                    db.miscInfoDao().updateRowData(miscInfo);
                }

                EditMiscInfoDialogFragment.this.getDialog().dismiss();

                // TODO: 5/2/18 fix refresh list
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                EditMiscInfoDialogFragment.this.getDialog().cancel();
            }
        });
        return builder.create();
    }

    public void setAdapter(PersonInfoRVAdapter adapter)
    {
        this.adapter = adapter;
    }
}
