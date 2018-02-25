package com.example.mehmet.andlit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.mehmet.andlit.helperUI.UICloudHelper;
import com.example.mehmet.andlit.helperUI.UILocalDBHelper;

/**
 * Created by Mehmet on 2/12/2018.
 */

public class SignUpActivity extends AppCompatActivity {

    UILocalDBHelper uiLocalDBHelper;
    UICloudHelper uiCloudHelper;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiLocalDBHelper = new UILocalDBHelper(this);
        uiCloudHelper = new UICloudHelper();
        setContentView(R.layout.activity_signup);
        initButtons();

    }

    private void initButtons(){

        Button registerButton = (Button) findViewById(R.id.register_new_button);
        registerButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String username = ((EditText)findViewById(R.id.new_username_input)).getText().toString();
                        final String password = ((EditText)findViewById(R.id.new_password_input)).getText().toString();
                        final String email = ((EditText)findViewById(R.id.new_email_input)).getText().toString();
                        Runnable r =  new Runnable() {
                            @Override
                            public synchronized void run() {
                                Bundle args = new Bundle();
                                args.putInt("register_success", register(email, username, password));
                                Message msg = new Message();
                                msg.setData(args);
                                SignUpHandler.sendMessage(msg);
                            }
                        };
                        Thread thread = new Thread(r);
                        thread.start();
                    }
                }
        );

        Button goBackButton = (Button) findViewById(R.id.back_to_login_button);
        goBackButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
        );
    }


    public int register(String email, String username, String password){
        int result = isValidSubmission(email, username, password);
        if(result != 1)
            return result;
        return uiCloudHelper.userSignUp(email, username, password);
    }

    public void backToLogin(){

    }

    private int isValidSubmission(String email, String username, String pw){
        return 1;
        /*
        if(!isEmailValid(email))
            return -2;
        if(username.length() < 6)
            return -1;
        if(pw.length() < 6 )
            return -3;
        return 1;*/
    }

    boolean isEmailValid(CharSequence email) {
        return true;
        //return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private synchronized void showPopUp(int status){
        if (status == -2){
            new AlertDialog.Builder(this)
                    .setMessage("Something went wrong with email")
                    .setPositiveButton("OK", null)
                    .show();
        }
        else if (status == -1){
            new AlertDialog.Builder(this)
                    .setMessage("Something went wrong with username")
                    .setPositiveButton("OK", null)
                    .show();
        }
        else if (status == -3){
            new AlertDialog.Builder(this)
                    .setMessage("Password is too short")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }



    Handler SignUpHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle messageInfo = msg.getData();
            int registerInfo = messageInfo.getInt("register_success");
            if(registerInfo < 0){
                showPopUp(registerInfo);
            }
            else{
                //start new intent with newly acquired id and stuff
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        }
    };
}
