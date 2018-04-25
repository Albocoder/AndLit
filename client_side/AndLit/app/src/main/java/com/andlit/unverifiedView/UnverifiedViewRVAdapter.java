package com.andlit.unverifiedView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.andlit.R;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.KnownPPL;
import com.andlit.database.entities.detected_face;
import com.andlit.database.entities.misc_info;
import com.andlit.database.entities.training_face;
import com.andlit.face.FaceOperator;
import com.andlit.face.FaceRecognizerSingleton;
import com.andlit.face.RecognizedFace;
import com.andlit.trainingView.TrainingViewRVAdapter;
import com.andlit.ui.helperUI.listRelated.PersonDataAdapter;
import com.andlit.ui.helperUI.listRelated.PotentialPeopleAdapter;
import com.andlit.ui.helperUI.listRelated.TwoStringDataHolder;
import com.andlit.utils.StorageHelper;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UnverifiedViewRVAdapter extends TrainingViewRVAdapter
{
    List<detected_face> persons;
    List<KnownPPL> allKnownPpl;

    UnverifiedViewRVAdapter(List<detected_face> persons,List<KnownPPL> known)
    {
        super(null);
        allKnownPpl = known;
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
                notifyItemRangeChanged(position, persons.size());
            }
        });

        personViewHolder.personPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // To Do: dialog appearance
                detected_face df = persons.get(position);// gives the selected detected_face
                showPopUpForFace(df,context,position);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showPopUpForFace(final detected_face df,final Context c,final int position) {
        final Dialog dialog = new Dialog(c);
        AppDatabase db = AppDatabase.getDatabase(c);
        dialog.setContentView(R.layout.user_face_profile_dialogue);
        String title;
        Bitmap photo = BitmapFactory.decodeFile(FaceOperator.getAbsolutePath(c,df));
        PersonDataAdapter pdAdapter = null;
        int bestPrediction = df.predictedlabel;
        if(bestPrediction == -1) {
            title = "Unknown Person";
            pdAdapter = new PersonDataAdapter(c,new ArrayList<TwoStringDataHolder>());
            pdAdapter.add(new TwoStringDataHolder("Full name", "Unknown"));
            pdAdapter.add(new TwoStringDataHolder("Date of birth","Unknown"));
            pdAdapter.add(new TwoStringDataHolder("Age","Unknown"));
        }
        else {
            KnownPPL p = db.knownPplDao().getPersonWithID(bestPrediction);
            title =  p.name+" "+p.sname;
            pdAdapter = getKnownDataForKnownPerson(p,c);
        }

        dialog.setTitle(title);
        ImageView profilePhoto = dialog.findViewById(R.id.profilePhoto);
        profilePhoto.setImageBitmap(photo);
        TextView name = dialog.findViewById(R.id.profileName);
        name.setText(title);

        ListView allKnown = dialog.findViewById(R.id.potentialCandidates);
        allKnown.setAdapter(new PotentialPeopleAdapter(c, allKnownPpl));
        allKnown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView personName = view.findViewById(R.id.name);
                final TextView personID = view.findViewById(R.id.description);
                String idString = personID.getText().toString();
                int id = Integer.parseInt(idString.substring(3));
                alertSettingIDForFace(df,c,personName.getText().toString(),id,dialog,position);
            }
        });

        ListView userData = dialog.findViewById(R.id.profileInfo);
        userData.setAdapter(pdAdapter);
        Button addNew = dialog.findViewById(R.id.addNew);
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                addNewFace(df,c,position);
            }
        });
        Button close = dialog.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void alertSettingIDForFace(final detected_face df,final Context c, String newPerson,
                                       final int i, final Dialog parent,final int position) {
        new AlertDialog.Builder(c)
                .setTitle("Was it really wrong?")
                .setMessage(Html.fromHtml("Saving this face as <b>"+newPerson
                        +"</b>?<br/><b>WARNING</b>: <i>Wrong person might compromise the accuracy.</i>"))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        df.id = i;
                        if(FaceOperator.moveDetectionToTraining(c,df)==null){
                            new AlertDialog.Builder(c).setTitle("Error!")
                                    .setMessage("Couldn't label the face")
                                    .setIcon(android.R.drawable.ic_dialog_alert).show();
                            return;
                        }
                        if(parent != null)
                            parent.dismiss();
                        persons.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, persons.size());
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void addNewFace(final detected_face df ,final Context c,final int position) {
        final Dialog dialog = new Dialog(c);
        dialog.setContentView(R.layout.add_new_person_layout);
        ImageView profileImage = dialog.findViewById(R.id.profileImage);
        Bitmap tmpb = BitmapFactory.decodeFile(FaceOperator.getAbsolutePath(c,df));
        profileImage.setImageBitmap(tmpb);
        final TextView name = dialog.findViewById(R.id.newName);
        final TextView sname = dialog.findViewById(R.id.newSname);
        Button save = dialog.findViewById(R.id.newPersonButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String n = name.getText().toString();
                String s = sname.getText().toString();
                if(n.length() <= 0 || s.length() <= 0)
                    Toast.makeText(view.getContext(),
                            "Name and Surname fields must be completed!", Toast.LENGTH_SHORT).show();
                else {
                    KnownPPL newPerson = new KnownPPL(-1,n,s,0,0,"");
                    AppDatabase db = AppDatabase.getDatabase(view.getContext());
                    long id = db.knownPplDao().insertEntry(newPerson);
                    df.id = ((int)id);
                    training_face f = FaceOperator.moveDetectionToTraining(c,df);
                    if( f == null )
                        Snackbar.make(view,"Couldn't save instance to database!",Snackbar.LENGTH_SHORT);
                    else{
                        allKnownPpl.add(newPerson);
                        persons.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, persons.size());
                    }
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private PersonDataAdapter getKnownDataForKnownPerson(KnownPPL p,Context c) {
        if (p.id <= 0)
            return null;
        AppDatabase db = AppDatabase.getDatabase(c);
        PersonDataAdapter toReturn= new PersonDataAdapter(c,new ArrayList<TwoStringDataHolder>());
        toReturn.add(new TwoStringDataHolder("Full name", p.name+" "+p.sname));
        if(p.dob != null)
            toReturn.add(new TwoStringDataHolder("Date of birth",new Date(p.dob).toString()));
        if(p.age != null)
            toReturn.add(new TwoStringDataHolder("Age",""+p.age));
        List<misc_info> infos = db.miscInfoDao().getInfosForID(p.id);
        for(misc_info i: infos)
            toReturn.add(new TwoStringDataHolder(i.key,i.desc));
        return toReturn;
    }

    @Override
    public int getItemCount()
    {
        return persons.size();
    }
}
