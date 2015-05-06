package com.example.quinn.m3ustreamtest2;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * http://199.255.3.11:88/broadwave.m3u?src=2&rate=1    Community Radio HD3
 * http://199.255.3.11:88/broadwave.m3u?src=1&rate=1     Jazz HD2
 * http://sportsweb.gtc.edu:8000/Sportsweb.m3u     Sports
 * http://199.255.3.11:88/broadwave.m3u?src=4&rate=1    Reading Service
 * http://media.gtc.edu:8000/stream.m3u     Public Radio HD1
 */
public class AudioPlayerActivity extends Activity {
    public static Context mContext;
    public static AudioPlayerActivity mActivity;

    public ListPlayer mListPlayer;
    private ImageButton mStartStopButton;
    private ImageButton mPrevButton;
    private ImageButton mNextButton;

    private TextView currentStationTextView;
    private TextView nextStationTextView;
    private TextView previousStationTextView;


    private StationSource []  mStations = {new StationSource("Public", R.string.public_radio_string, "http://media.gtc.edu:8000/stream.m3u"), //Public Radio
            new StationSource("Jazz",R.string.jazz_station_string, "http://199.255.3.11:88/broadwave.m3u?src=1&rate=1"),//Jazz
            new StationSource("Reading",R.string.reading_service_string, "http://199.255.3.11:88/broadwave.m3u?src=4&rate=1" ), //Reading Service
            new StationSource("Sports", R.string.sports_station_string, "http://sportsweb.gtc.edu:8000/Sportsweb.m3u"), //Sports
            new StationSource("Community",R.string.community_station_string, "http://199.255.3.11:88/broadwave.m3u?src=2&rate=1")}; //Community Radio

    private int mCurrentIndex;

    private boolean mPlaying;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_audio_player, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentIndex = 0;
        //ConnectionTest x = new ConnectionTest(this);
        //x.execute();
        mContext = this;
        mActivity = this;
        mPlaying = false;

        setContentView(R.layout.main_act_layout);

        currentStationTextView = (TextView) findViewById(R.id.current_station_banner);
        nextStationTextView = (TextView) findViewById(R.id.next_station_text_view);
        previousStationTextView = (TextView) findViewById(R.id.prev_station_text_view);

        mStartStopButton = (ImageButton) findViewById(R.id.plav_pause_button);
        mStartStopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(mPlaying) {
                    setPlayer(false);
                    mStartStopButton.setBackgroundResource(R.drawable.play_red);
                } else {
                    setPlayer(true);
                    mStartStopButton.setBackgroundResource(R.drawable.pause_red);
                }
                mPlaying = ! mPlaying;
                //mCurrentIndex = mCurrentIndex+1% mStations.length;
            }
        });

        mNextButton = (ImageButton) findViewById(R.id.next_station_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCurrentIndex = (mCurrentIndex+1)% mStations.length;
                updateTextViews();
                setPlayer(true);

            }
        });

        mPrevButton = (ImageButton) findViewById(R.id.previous_station_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCurrentIndex = ((mCurrentIndex-1) + mStations.length)% mStations.length;
                updateTextViews();
                setPlayer(true);


            }
        });

        getFragmentManager().beginTransaction()
                .add(R.id.wave_container, new WaveFragment())
                .commit();

    }

    private void updateTextViews(){
        previousStationTextView.setText(getString(mStations[((mCurrentIndex-1) + mStations.length)% mStations.length].getResourceID()));
        currentStationTextView.setText(getString(mStations[mCurrentIndex].getResourceID()));
        nextStationTextView.setText(getString(mStations[(mCurrentIndex+1)% mStations.length].getResourceID()));
    }


    private void setPlayer(boolean needToStart){
        if (needToStart) {
                if(mPlaying){
                    mListPlayer.stopPlaying();
                }
            startPlayer(mStations[mCurrentIndex].getSource());

        } else {

            mListPlayer.stopPlaying();

        }
    }

    private void startPlayer(String source){
        Intent intent = getIntent();
        // Are we called from main or by our M3U intent?
        if (intent.getAction().equals(Intent.ACTION_MAIN)) {
            System.out.println("Is main");
            new DownloadM3U(this).execute(source);
            //"http://199.255.3.11:88/broadwave.m3u?src=2&rate=1"
            //http://199.255.3.11:88/broadwave.m3u?src=2&rate=1
        } else
        if (intent != null && intent.getData() != null) {
            new DownloadM3U(this).execute(intent.getData().toString());
        }
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        System.out.println("External Player finished");
        synchronized(mListPlayer) {
            mListPlayer.notifyAll();
        }
    }

    public Context getContext(){
        return this.getBaseContext();
    }


    public class WaveFragment extends Fragment {

        public float halfHeight;// = screenHeight/2.0f;
        public float width;// = screenWidth;
        public float height;
        public AudioManager am;

        public WaveView line;

        public WaveFragment() {
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            halfHeight = container.getHeight() / 2;
            height = container.getHeight();
            width = container.getWidth();
            line = new WaveView(getActivity());
            final AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);

            Timer timer = new Timer();

            TimerTask task = new TimerTask() {

                synchronized public void run() {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //int volume_level=

                            line.updateWaveWithLevel(0.05f * am.getStreamVolume(AudioManager.STREAM_MUSIC));
                        }
                    });

                }
            };

            timer.scheduleAtFixedRate(task, 0, 15);

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
                numberOfWaves = 5;
                amplitude = 1.0;
                idleAmplitude = 0.01;
            }

            public void updateWaveWithLevel(float level) {
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

}
