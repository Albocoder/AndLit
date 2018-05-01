package com.andlit.ui.classifierView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.andlit.R;
import com.andlit.face.FaceRecognizerSingleton;

public class ClassifierViewActivity extends AppCompatActivity
{
    Context context;
    ClassifierViewRVAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_classifier_view);

        RecyclerView rv = findViewById(R.id.rvClassifierView);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        rv.setHasFixedSize(true);

        adapter = new ClassifierViewRVAdapter();
        rv.setAdapter(adapter);

        context = this.getApplicationContext();

        Button trainNow = findViewById(R.id.train_now_button);
        trainNow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Train now button task
                new TrainNowTask().execute();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class TrainNowTask extends AsyncTask<String, Void, Integer>
    {
        private ProgressDialog progressDialog = new ProgressDialog(ClassifierViewActivity.this
                ,R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute()
        {
            // Display the loading spinner
            progressDialog.setMessage("Training Classifier...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setInverseBackgroundForced(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... paramsObj)
        {
            try
            {
                FaceRecognizerSingleton frc = new FaceRecognizerSingleton(context);
                frc.trainModel();
            }
            catch( Exception e )
            {
                return 2;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer ret)
        {
            if(ret == 0)
            {
                adapter.notifyDataSetChanged();

                Toast toast = Toast.makeText(context, "Successfully Trained Classifier!", Toast.LENGTH_SHORT);
                toast.show();
            }
            else
            {
                Toast toast = Toast.makeText(context, "Couldn't Train Classifier!", Toast.LENGTH_SHORT);
                toast.show();
            }

            progressDialog.dismiss();
        }
    }
}



