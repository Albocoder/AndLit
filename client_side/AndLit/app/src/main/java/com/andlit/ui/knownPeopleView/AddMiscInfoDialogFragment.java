package com.andlit.ui.knownPeopleView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.andlit.R;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.misc_info;

public class AddMiscInfoDialogFragment extends DialogFragment
{
    PersonInfoRVAdapter adapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.add_misc_info_dialog, null);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Add Info", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // Add info
                        String personId = getArguments().getString("PERSON_ID");

                        EditText descField = view.findViewById(R.id.desc);
                        EditText infoField = view.findViewById(R.id.info_edit);

                        String desc = descField.getText().toString();
                        String info = infoField.getText().toString();

                        AppDatabase db = AppDatabase.getDatabase(getContext());
                        misc_info miscInfo = new misc_info(Integer.parseInt(personId), desc, info);

                        try
                        {
                            db.miscInfoDao().insertMiscInfo(miscInfo);
                        }
                        catch(RuntimeException e)
                        {
                            Toast toast = Toast.makeText(view.getContext(), "Description Already Exists!", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        AddMiscInfoDialogFragment.this.getDialog().dismiss();

                        // TODO: 5/2/18 fix refresh list
                        if(adapter != null)
                        {
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddMiscInfoDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    public void setAdapter(PersonInfoRVAdapter adapter)
    {
        this.adapter = adapter;
    }
}
