package com.appfactory.quinn.m3ustreamtest2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.AudioManager;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Timer;
import java.util.TimerTask;

public class WaveFragment extends Fragment {
    public float halfHeight;// = screenHeight/2.0f;
    public float width;// = screenWidth;
    public float height;
    public AudioManager am;
    AudioPlayerActivity parent;

    public WaveView line;

    public static WaveFragment newInstance(String param1, String param2) {
        WaveFragment fragment = new WaveFragment();
        Bundle args = new Bundle();

        return fragment;
    }

    public WaveFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parent.updateViews();

        halfHeight = container.getHeight() / 2;
        height = container.getHeight();
        width = container.getWidth();
        line = new WaveView(getActivity());
        final AudioManager am = (AudioManager) parent.getSystemService(parent.AUDIO_SERVICE);
        int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        parent.timer = new Timer();

        parent.task = new TimerTask() {

            synchronized public void run() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //int volume_level=
                        double level = (float) (20 * Math.log10(parent.mRecorder.getMaxAmplitude() / 700.0));

                        if (parent.doneBuffering) {
                            if (parent.intensity == 0.103975) {

                            }

                            if (parent.intensity < 0.001) {
                                line.updateWaveWithLevel(0.1);
                            } else if (parent.intensity < 0.5) {
                                line.updateWaveWithLevel(0.5);
                            } else {
                                line.updateWaveWithLevel(0.8);
                            }
                        } else {
                            line.updateWaveWithLevel(0.01);
                        }
                    }
                });
            }
        };

        parent.timer.scheduleAtFixedRate(parent.task, 0, 10);

        return line;
    }

    public class WaveView extends View {
        private double phase;
        private double phaseShift;
        private double primaryWaveLength;
        private double secondaryWaveLength;
        public int numberOfWaves;
        public double amplitude;
        public double idleAmplitude;

        public WaveView(Context context) {

            super(context);

            this.phaseShift = -0.15;
            primaryWaveLength = 3.0f;
            secondaryWaveLength = 1.0f;
            numberOfWaves = 1;
            amplitude = 1.0;
            idleAmplitude = 0.01;
        }

        public void updateWaveWithLevel(double level) {
            phase += phaseShift;
            amplitude = Math.max(level, idleAmplitude);

            invalidate();
        }

        @Override

        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            Paint paint = new Paint();

            paint.setColor(Color.parseColor("#B62455"));

            paint.setStrokeWidth(6);

            paint.setStyle(Paint.Style.STROKE);

            Path path = new Path();

            for (int i = 0; i < numberOfWaves; i++) {
                if (i != 0) {
                    paint.setStrokeWidth(1.0f);
                }

                float halfHeight = canvas.getHeight() / 2.0f;//screenHeight/2.0f;
                float width = canvas.getWidth();//screenWidth;
                float mid = width / 2.0f;

                float progress = 1.0f - (float) i / numberOfWaves;

                double normedAmplitude = (1.5f * progress - 0.5f) * amplitude;
                float maxAmplitude = halfHeight - 4.0f;

                float multiplier = Math.min(1.0f, (progress / 3.0f * 2.0f) + (1.0f / 3.0f));
                int waveColorInt = paint.getColor();
                int newAlpha = (int) (Color.alpha(waveColorInt) * multiplier);
                int waveColor = Color.argb(newAlpha, Color.red(waveColorInt), Color.green(waveColorInt), Color.blue(waveColorInt));
                paint.setColor(waveColor);

                int density = 5;

                for (float x = 0; x < width + 5; x += 5) {
                    // We use a parable to scale the sinus wave, that has its peak in the middle of the view.
                    double scaling = -Math.pow(1 / mid * (x - mid), 2) + 1;

                    float y = (float) (scaling * maxAmplitude * normedAmplitude * Math.sin(2 * Math.PI * (x / width) * 1.5 + this.phase) + halfHeight);

                    if (x == 0) {
                        path.moveTo(x, y);
                    } else {
                        path.lineTo(x, y);
                    }
                }

                canvas.drawPath(path, paint);
            }
        }
    }
}
