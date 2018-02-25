package com.example.mehmet.andlit.helperUI;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mehmet.andlit.MainActivity;
import com.example.mehmet.andlit.R;

/**
 * Created by Mehmet on 1/26/2018.
 */

public class TakePhotoFragment extends Fragment {
    @Nullable

    MainActivity mainActivity;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.live_feed, container, false);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity.findViewById(R.id.take_photo_feed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.switchFragments(R.id.content_frame, new ShowImageFragment());
            }
        });
    }

    void setMainActivity(MainActivity ma){
        mainActivity = ma;
    }

}
