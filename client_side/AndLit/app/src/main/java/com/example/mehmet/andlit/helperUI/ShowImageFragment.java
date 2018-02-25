package com.example.mehmet.andlit.helperUI;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mehmet.andlit.R;

/**
 * Created by Mehmet on 1/21/2018.
 */

public class ShowImageFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.image_view_layout, container, false);
    }


}
