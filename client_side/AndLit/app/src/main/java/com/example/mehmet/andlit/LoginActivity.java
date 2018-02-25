package com.example.mehmet.andlit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.mehmet.andlit.helperUI.UICloudHelper;
import com.example.mehmet.andlit.helperUI.UILocalDBHelper;

/**
 * Created by Mehmet on 1/21/2018.
 */

public class LoginActivity extends AppCompatActivity {

    UILocalDBHelper uiLocalDBHelper;
    UICloudHelper uiCloudHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiLocalDBHelper = new UILocalDBHelper(this);
        uiCloudHelper = new UICloudHelper();

        if(!uiLocalDBHelper.getAskForPassword()){
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        }
        setContentView(R.layout.activity_login);
        initButtons();

    }

    private void initButtons(){

        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String username = ((EditText)findViewById(R.id.username_input)).getText().toString();
                        final String password = ((EditText)findViewById(R.id.password_input)).getText().toString();
                        Runnable r =  new Runnable() {
                            @Override
                            public void run() {
                                Bundle args = new Bundle();
                                args.putBoolean("login_success", login(username, password));
                                Message msg = new Message();
                                msg.setData(args);
                                LoginHandler.sendMessage(msg);
                            }
                        };
                        Thread thread = new Thread(r);
                        thread.start();
                    }
                }
        );

        Button signUpButton = (Button) findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getApplicationContext(), SignUpActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
        );
    }

    public boolean login(String username, String pw){
        Bundle result = uiCloudHelper.userLogin(username, pw);
        if(result.getBoolean("login_result")){
            uiLocalDBHelper.setUserID(result.getInt("user_id"));
            return true;
        }
        return false;
    }



    Handler LoginHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle messageInfo = msg.getData();
            if(messageInfo.getBoolean("login_success")) {
                uiLocalDBHelper.setAskForPassword(false);
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
            else {
            }
        }
    };

}
