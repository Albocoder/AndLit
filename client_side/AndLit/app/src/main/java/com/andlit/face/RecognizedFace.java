package com.andlit.face;


import android.content.Context;

import com.andlit.database.AppDatabase;
import com.andlit.database.entities.KnownPPL;
import com.andlit.database.entities.misc_info;

import java.util.List;

public class RecognizedFace {
    private Face f;
    private int [] labels;
    private double [] confidence;
    private KnownPPL bestMatch;
    private List<misc_info> bestMisc;

    public RecognizedFace(Face f, int [] l, double [] c){
        if (l.length != c.length)
            throw new RuntimeException("labels must be same number as confidences");
        if(l.length <= 0 || c.length <= 0)
            throw new RuntimeException("must have at least 1 label");
        this.f = f;
        labels = l;
        bestMatch = null;
        bestMisc = null;
        confidence = c;
    }

    public int [] getLabels(){return labels;}
    public double[] getConfidences(){return confidence;}
    public Face getFace(){return f;}

    public void setBestMatch(Context c) {
        if(bestMatch != null || labels.length <= 0)
            return;
        if(labels[0] <= 0)
            return;
        AppDatabase db = AppDatabase.getDatabase(c);
        bestMatch = db.knownPplDao().getEntryWithID(labels[0]);
        bestMisc = db.miscInfoDao().getInfosForID(labels[0]);
    }
    public KnownPPL getBestMatch() { return bestMatch; }
    public List<misc_info> getMisc() { return bestMisc; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("< (label,confidence) = { ");
        for (int i = 0; i < labels.length; i++)
            sb.append("(").append(labels[i]).append(",").append(confidence[i]).append(") ");
        sb.append("}");
        return sb.toString();
    }
}
