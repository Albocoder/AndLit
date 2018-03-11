package com.example.mehmet.andlit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mehmet.andlit.CloudInterface.Authenticator;
import com.example.mehmet.andlit.database.entities.UserLogin;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    // constants
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    // fields
    private Authenticator a;
    @BindView(R.id.input_email)
    EditText _emailText;
    @BindView(R.id.input_password)
    EditText _passwordText;
    @BindView(R.id.btn_login)
    Button _loginButton;
    @BindView(R.id.link_signup)
    TextView _signupLink;

    // view related
    private AnimationDrawable animationDrawable;
    private ScrollView sv;


    // ******************************** VIEW RELATED FUNCTIONS ******************************** //
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        a = new Authenticator(this);

        if(a.isLoggedIn())
            goToHomePage();

        sv = (ScrollView)findViewById(R.id.loginLayout);
        animationDrawable = (AnimationDrawable) sv.getBackground();
        animationDrawable.setEnterFadeDuration(5000);
        animationDrawable.setExitFadeDuration(2000);



        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (animationDrawable != null && !animationDrawable.isRunning())
            animationDrawable.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (animationDrawable != null && animationDrawable.isRunning())
            animationDrawable.stop();
    }

    // if signup is successful we go to home page and finish this
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                goToHomePage();
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    // ****************************** OPERATIONS RELATED FUNCTIONS ****************************** //
    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed("Invalid form data!");
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        final String username = _emailText.getText().toString();
        final String password = _passwordText.getText().toString();

        new LoginTask().execute(username,password);
    }

    private void goToHomePage() {
        // todo: add logic for going to home page
        finish();
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        goToHomePage();
        finish();
    }

    public void onLoginFailed(String msg) {
        Toast.makeText(getBaseContext(), "Error: "+msg, Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String username = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (username.isEmpty() || !username.matches("[A-Za-z0-9_]+")) {
            _emailText.setError("enter a valid username");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() <= 6 || password.length() >= 20) {
            _passwordText.setError("between 6 and 20 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    private class LoginTask extends AsyncTask<String, Void, Void>
    {

        private ProgressDialog Dialog = new ProgressDialog(LoginActivity.this,R.style.AppTheme_Dark_Dialog);

        protected void onPreExecute() {
            // Display the loading spinner
            Dialog.setMessage("Authenticating.. ");
            Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            Dialog.setInverseBackgroundForced(false);
            Dialog.setIndeterminate(true);
            Dialog.setCancelable(false);
            Dialog.show();
        }

        @Override
        protected Void doInBackground(String... paramsObj) {
            try {
                UserLogin ul = a.login(paramsObj[0],paramsObj[1]);
                if ( ul == null ) {
                    Log.d(TAG,"Wrong credentials!");
                    return null;
                }
            } catch ( IOException e ) {
                Log.d(TAG,"No internet connection!");
                return null;
            }
            // On complete call either onLoginSuccess or onLoginFailed
            onLoginSuccess();
            return null;
        }

        protected void onPostExecute(Void unused) {
            Dialog.dismiss();
        }
    }
}
