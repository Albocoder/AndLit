package com.example.mehmet.andlit.helperUI;

import android.media.Image;
import android.util.Log;

import com.example.mehmet.andlit.MainActivity;
import com.example.mehmet.andlit.R;

/**
 * Created by Mehmet on 1/26/2018.
 */

public class UIHardwareHelper {
    MainActivity mainActivity;
    public UIHardwareHelper(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public int takeImage(){

        try{
            Thread.sleep(3000);
        } catch(Exception e){
            String strace = "";
            for(int i = 0; i < e.getStackTrace().length; i++){
                strace += e.getStackTrace()[i].toString() + "\n";
            }
            Log.d("papa", "takeImage: " + strace);
        }
        return R.drawable.image_to_show;
    }
}
