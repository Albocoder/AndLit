package com.andlit.helperUI.listRelated;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andlit.R;

import java.util.List;

public class PersonDataAdapter extends ArrayAdapter<TwoStringDataHolder> implements View.OnClickListener{

    public PersonDataAdapter(Context c, List<TwoStringDataHolder> strings) {
        super(c,0,strings);
    }

    @Override
    public void onClick(View view) {}

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TwoStringDataHolder p = getItem(position);
        if (p == null)
            return convertView;
        View vi = convertView;
        if(convertView==null)
            vi = LayoutInflater.from(getContext()).inflate(R.layout.user_data_list_item_layout,
                    parent, false);
        TextView key = (TextView) vi.findViewById(R.id.key);
        TextView val = (TextView) vi.findViewById(R.id.value);
        key.setText(p.s1);
        val.setText(p.s2);

        return vi;
    }
}
