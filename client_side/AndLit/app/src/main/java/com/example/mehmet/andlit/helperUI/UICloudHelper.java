package com.example.mehmet.andlit.helperUI;

import android.os.Bundle;

/**
 * Created by Mehmet on 2/12/2018.
 */

public class UICloudHelper {

    public Bundle userLogin(String user, String pw){
        Bundle bnd = new Bundle();
        bnd.putBoolean("login_result", true);
        bnd.putInt("user_id" , -1);
        return bnd;
    }

    public synchronized int userSignUp(String email, String user, String pw){
        /*if email already exists return -2
        if username already exists return -1
        else return user id that just created
         */
        return 1;
    }

    boolean checkUsernameIfExists(String username){
        /*
        if exists return 1
        else return 0
         */
        return false;
    }
}
