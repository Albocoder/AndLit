package com.andlit.groupView;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.andlit.R;

public class CreateGroupFragment extends Fragment
{
    public CreateGroupFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_create_group, container, false);

        Button button = view.findViewById(R.id.button_create_group);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                // TODO: 4/26/18 what happens when you press create group button
                // don't forget to change the status and clear fields

            }
        });

        return view;
    }
}
