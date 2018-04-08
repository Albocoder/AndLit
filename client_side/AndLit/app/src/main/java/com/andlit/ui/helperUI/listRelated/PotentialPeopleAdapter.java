package com.andlit.ui.helperUI.listRelated;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.andlit.R;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.KnownPPL;
import com.andlit.database.entities.training_face;

import java.util.List;

import static java.lang.String.format;

public class PotentialPeopleAdapter extends ArrayAdapter<KnownPPL> {
    private static final String TAG = "PotentialPeopleAdapter";

    public PotentialPeopleAdapter(Context c,List<KnownPPL> kp) {
        super(c,0,kp);
    }

    @SuppressLint("DefaultLocale")
    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        KnownPPL p = getItem(position);
        if (p == null)
            return convertView;
        View vi = convertView;
        if(convertView==null)
            vi = LayoutInflater.from(getContext()).inflate(R.layout.known_person_list_item,
                    parent, false);
        TextView name = (TextView) vi.findViewById(R.id.name);
        TextView desc = (TextView) vi.findViewById(R.id.description);
        ImageView pic = (ImageView) vi.findViewById(R.id.picture);

        name.setText(format("%s %s", p.name, p.sname));
        desc.setText(format("ID:%d", p.id));
        AppDatabase db = AppDatabase.getDatabase(getContext());
        List<training_face> images = db.trainingFaceDao().getInstancesOfLabel(p.id);
        Bitmap tmp;
        if (images.size() != 0) {
            tmp = BitmapFactory.decodeFile(images.get(0).path);
            pic.setImageBitmap(tmp);
        }
        // todo add actions
        return vi;
    }
}
