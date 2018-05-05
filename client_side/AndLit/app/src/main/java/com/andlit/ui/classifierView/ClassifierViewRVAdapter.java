package com.andlit.ui.classifierView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.andlit.R;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.Classifier;

public class ClassifierViewRVAdapter extends RecyclerView.Adapter<com.andlit.ui.classifierView.ClassifierViewRVAdapter.ClassifierViewHolder>
{
    ClassifierViewRVAdapter()
    {

    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // provide access to all the views for a data item in a view holder
    static class ClassifierViewHolder extends RecyclerView.ViewHolder
    {
        CardView cv;
        TextView classifierStatDesc;
        TextView classifierStatItem;

        ClassifierViewHolder(View itemView)
        {
            super(itemView);
            cv = itemView.findViewById(R.id.cvClassifierItem);
            classifierStatDesc = itemView.findViewById(R.id.classifier_stat_desc);
            classifierStatItem = itemView.findViewById(R.id.classifier_stat_item);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public com.andlit.ui.classifierView.ClassifierViewRVAdapter.ClassifierViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.classifier_view_list_item, viewGroup, false);
        return new ClassifierViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final com.andlit.ui.classifierView.ClassifierViewRVAdapter.ClassifierViewHolder viewHolder, final int position)
    {
        Context context = viewHolder.itemView.getContext();
        AppDatabase db = AppDatabase.getDatabase(context);
        Classifier classifierMetadata = AppDatabase.getDatabase(context).classifierDao().getClassifier();
        switch( position )
        {
            case 0:
                viewHolder.classifierStatDesc.setText("No. of Recognizable Faces: ");
                if( classifierMetadata != null)
                {
                    int numberOfCurrentDetections = classifierMetadata.num_recogn;
                    viewHolder.classifierStatItem.setText("" + numberOfCurrentDetections);
                }
                break;
            case 1:
                viewHolder.classifierStatDesc.setText("No. of Instances Trained on: ");
                if( classifierMetadata != null )
                {
                    int numberOfInstancesUsed = classifierMetadata.num_inst_trained;
                    viewHolder.classifierStatItem.setText("" + numberOfInstancesUsed);
                }
                break;
            case 2:
                viewHolder.classifierStatDesc.setText("No. of Possible Instances: ");
                int numberOfInstancesPossible = db.trainingFaceDao().getNumberOfTrainingInstances();
                viewHolder.classifierStatItem.setText("" + numberOfInstancesPossible);
                break;
            case 3:
                viewHolder.classifierStatDesc.setText("No. of Possible Recognitions: ");
                int numberOfPossibleDetections = db.trainingFaceDao().getNumberOfPossibleRecognitions();
                viewHolder.classifierStatItem.setText("" + numberOfPossibleDetections);
                break;
        }

    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return 4;   // return the number of cards you want
    }
}


