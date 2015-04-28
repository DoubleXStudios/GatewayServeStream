package com.example.quinn.m3ustreamtest2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by Quinn on 4/28/15.
 */
public class LineGenerator extends View{

        private double phase;
        private double phaseShift;
        private double primaryWaveLength;
        private double secondaryWaveLength;
        public int numberOfWaves;
        public double amplitude;
        public double idleAmplitude;
        public int screenHeight;
        public int screenWidth;


        public LineGenerator(Context context) {

            super(context);
            DisplayMetrics metrics = new DisplayMetrics();

            this.phaseShift = -0.15;
            primaryWaveLength = 3.0f;
            secondaryWaveLength = 1.0f;
            numberOfWaves = 5;
            amplitude = 1.0;
            idleAmplitude = 0.01;
            this.screenHeight = metrics.heightPixels;
            this.screenWidth = metrics.widthPixels;
        }

        public void updateWaveWithLevel(float level)
        {
            phase += phaseShift;
            amplitude = Math.max(level, idleAmplitude);

            invalidate();
        }


        @Override

        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            Paint paint = new Paint();

            paint.setColor(Color.RED);

            paint.setStrokeWidth(3);

            paint.setStyle(Paint.Style.STROKE);

            Path path = new Path();

            for(int i = 0; i < numberOfWaves; i++)
            {
                if(i != 0)
                {
                    paint.setStrokeWidth(1.0f);
                }

                float halfHeight = screenHeight/2.0f;
                float width = screenWidth;
                float mid = width / 2.0f;

                float progress = 1.0f - (float)i / numberOfWaves;

                double normedAmplitude = (1.5f * progress - 0.5f) * amplitude;
                float maxAmplitude = halfHeight - 4.0f;

                float multiplier = Math.min(1.0f, (progress / 3.0f * 2.0f) + (1.0f / 3.0f));
                int waveColorInt = paint.getColor();
                int newAlpha = (int)(Color.alpha(waveColorInt) * multiplier);
                int waveColor = Color.argb(newAlpha, Color.red(waveColorInt), Color.green(waveColorInt), Color.blue(waveColorInt));
                paint.setColor(waveColor);

                int density = 5;

                for (float x = 0; x<width + 5; x += 5) {
                    // We use a parable to scale the sinus wave, that has its peak in the middle of the view.
                    double scaling = -Math.pow(1 / mid * (x - mid), 2) + 1;

                    float y = (float)(scaling * maxAmplitude * normedAmplitude * Math.sin(2 * Math.PI *(x / width) * 1.5 + this.phase) + halfHeight);

                    if(x == 0)
                    {
                        path.moveTo(x, y);
                    } else
                    {
                        path.lineTo(x, y);
                    }
                }

                canvas.drawPath(path, paint);
            }

        }


}
