package com.andlit.ui.andlitDevice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andlit.R;

import java.util.ArrayList;

public class WifiSSIDAdapter extends ArrayAdapter<String> {
    private LayoutInflater inflater;
    private ArrayList<String> res;

    public WifiSSIDAdapter(@NonNull Context context, int resource, ArrayList<String> res) {
        super(context, resource,res);
        this.res = res;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public String getItem(int position) {
        String s = super.getItem(position);
        if(s == null)
            return null;
        return  s.substring(4);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = inflater.inflate(R.layout.spinner_devices_padded, parent, false);

        String apname = res.get(position);

        String enc = apname.substring(0,3);
        TextView name = row.findViewById(R.id.name_label);
        TextView address = row.findViewById(R.id.address_label);

        name.setText(apname.substring(4));
        address.setText(enc.equals("ENC")?"password protected":"free");
        return row;
    }

    @NonNull
    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = inflater.inflate(R.layout.spinner_devices, parent, false);

        String apname = res.get(position);
        String enc = apname.substring(0,3);
        TextView name = row.findViewById(R.id.name_label);
        TextView address = row.findViewById(R.id.address_label);

        name.setText(apname.substring(4));
        address.setText(enc.equals("ENC")?"password protected":"free");
        return row;
    }
}
