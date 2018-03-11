package com.example.mehmet.andlit.helperUI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;


public class CanvasView extends View {
    public int width;
    public int height;
    Bitmap bMap;
    Canvas canvas;
    Context context;
    ArrayList<Rect> list;

    public CanvasView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        list = new ArrayList<Rect>();
        list.add(new Rect(600, 700, 800, 900));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        bMap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bMap);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        Log.d("eyyyyfeyo", "onDraw: lul" + list.size());
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        for(int i = 0; i < list.size(); i++){
            canvas.drawCircle(100, 100 ,100 ,paint);
            canvas.drawRect(list.get(i), paint);
        }
    }

    public void setList(ArrayList<Rect> rlist){
        list = rlist;
    }
}
