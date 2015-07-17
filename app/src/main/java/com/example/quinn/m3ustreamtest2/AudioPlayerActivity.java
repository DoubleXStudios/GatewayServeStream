package com.example.quinn.m3ustreamtest2;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.gfranks.minimal.notification.GFMinimalNotification;
import com.github.gfranks.minimal.notification.GFMinimalNotificationStyle;
import com.github.gfranks.minimal.notification.activity.BaseNotificationActivity;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * http://199.255.3.11:88/broadwave.m3u?src=2&rate=1    Community Radio HD3
 * http://199.255.3.11:88/broadwave.m3u?src=1&rate=1     Jazz HD2
 * http://sportsweb.gtc.edu:8000/Sportsweb.m3u     Sports
 * http://199.255.3.11:88/broadwave.m3u?src=4&rate=1    Reading Service
 * http://media.gtc.edu:8000/stream.m3u     Public Radio HD1
 */
public class AudioPlayerActivity extends BaseNotificationActivity {
    public static Context mContext;
    public static AudioPlayerActivity mActivity;

    public MediaPlayer player;
    private ImageButton mStartStopButton;
    private ImageButton mPrevButton;
    private ImageButton mNextButton;

    private ImageView currentStationBanner;
    private TextView nextStationTextView;
    private TextView previousStationTextView;
    private GFMinimalNotification notification;
    private boolean doneBuffering;

    private Visualizer audioOutput = null;
    public float intensity = 0;

    private MediaRecorder mRecorder;

    private StationSource []  mStations = {new StationSource("Public", R.string.public_radio_string, "http://media.gtc.edu:8000/stream\n"), //Public Radio
            new StationSource("Jazz",R.string.jazz_station_string, "http://199.255.3.11:88/broadwave.mp3?src=1&rate=1&ref=http%3A%2F%2Fwww.wgtd.org%2Fhd2.asp"),//Jazz
            new StationSource("Reading",R.string.reading_service_string, "http://199.255.3.11:88/broadwave.mp3?src=4&rate=1&ref=http%3A%2F%2Fwww.wgtd.org%2Freading.asp" ), //Reading Service
            new StationSource("Sports", R.string.sports_station_string, "http://sportsweb.gtc.edu:8000/Sportsweb")}; //Sports

    private int[] bannerImages = {R.drawable.classical, R.drawable.jazz, R.drawable.reading, R.drawable.sports};
    private int mCurrentIndex;

    private boolean playPressed;
    private boolean preparing;
    private Timer timer;
    private TimerTask task;

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_audio_player, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        timer.cancel();
        timer = null;
        player.stop();
        System.exit(0);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(timer == null)
        {
            timer.scheduleAtFixedRate(task, 0, 10);
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-63784829-1"); // Replace with actual tracker/property Id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

        ActionBar actionBar;

        createVisualizer();

        actionBar = getActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#282828"));
        actionBar.setBackgroundDrawable(colorDrawable);

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile("/dev/null");

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRecorder.start();

        mCurrentIndex = 0;
        mContext = this;
        mActivity = this;
        playPressed = false;
        doneBuffering = false;

        setupPlayer();

        setContentView(R.layout.main_act_layout);

        currentStationBanner = (ImageView)findViewById(R.id.current_station_banner);
        nextStationTextView = (TextView) findViewById(R.id.next_station_text_view);
        previousStationTextView = (TextView) findViewById(R.id.prev_station_text_view);

        mStartStopButton = (ImageButton) findViewById(R.id.plav_pause_button);
        mStartStopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(playPressed) {

                    if(player.isPlaying())
                    {
                        player.stop();
                    }

                    mStartStopButton.setBackgroundResource(R.drawable.play_red);
                    doneBuffering = false;

                    if(notification != null)
                    {
                        notification.dismiss();
                    }
                } else {
                    setupPlayer();
                    new CheckInternetTask().execute();

                    mStartStopButton.setBackgroundResource(R.drawable.pause_red);

                    if(notification != null)
                    {
                        notification.dismiss();
                    }
                    notification = new GFMinimalNotification(mActivity, GFMinimalNotificationStyle.WARNING , "", "Your stream is loading....",
                            0);
                    notification.show(mActivity);
                }
                playPressed = ! playPressed;
                //mCurrentIndex = mCurrentIndex+1% mStations.length;
            }
        });

        mNextButton = (ImageButton) findViewById(R.id.next_station_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex+1)% mStations.length;
                updateTextViews();
                mStartStopButton.setBackgroundResource(R.drawable.play_red);

                playPressed = false;
                doneBuffering = false;

                if(player.isPlaying())
                {
                    player.stop();
                }

                setupPlayer();
            }
        });

        mPrevButton = (ImageButton) findViewById(R.id.previous_station_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCurrentIndex = ((mCurrentIndex - 1) + mStations.length) % mStations.length;
                updateTextViews();
                mStartStopButton.setBackgroundResource(R.drawable.play_red);

                playPressed = false;
                doneBuffering = false;

                if(player.isPlaying())
                {
                    player.stop();
                }

                setupPlayer();
            }
        });

        getFragmentManager().beginTransaction()
                .add(R.id.wave_container, new WaveFragment())
                .commit();

    }

    private void updateTextViews(){
        previousStationTextView.setText(getString(mStations[((mCurrentIndex-1) + mStations.length)% mStations.length].getResourceID()));
        currentStationBanner.setImageResource(bannerImages[mCurrentIndex]);
        nextStationTextView.setText(getString(mStations[(mCurrentIndex + 1) % mStations.length].getResourceID()));
    }

    private void setupPlayer()
    {
        player = new MediaPlayer();
        try
        {
            player.setDataSource(mStations[mCurrentIndex].getSource());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), String.format("%s", mStations[mCurrentIndex].getSource()), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (notification != null) {
                    notification.dismiss();
                }

                if (playPressed) {
                    notification = new GFMinimalNotification(mActivity, GFMinimalNotificationStyle.SUCCESS, "", "Your station is playing!");
                    notification.show(mActivity);

                    player.start();
                    doneBuffering = true;
                }
            }
        });

        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                if (playPressed) {
                    if (notification != null) {
                        notification.dismiss();
                    }
                    notification = new GFMinimalNotification(mActivity, GFMinimalNotificationStyle.ERROR, "", "There was an error!");
                    notification.show(mActivity);

                    mStartStopButton.setBackgroundResource(R.drawable.play_red);
                    doneBuffering = false;
                    playPressed = false;
                }

                for (int k = 0; i < 10; i++) {
                    Toast.makeText(getApplicationContext(), String.format("Error Code: %d", i), Toast.LENGTH_LONG).show();
                }

                return false;
            }
        });

    }

    private void createVisualizer(){
        int rate = Visualizer.getMaxCaptureRate();
        audioOutput = new Visualizer(0); // get output audio stream
        audioOutput.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                intensity = ((float) waveform[0] + 128f) / 256;
                Log.d("vis", String.valueOf(intensity));
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {

            }
        },rate , true, false); // waveform not freq data
        Log.d("rate", String.valueOf(Visualizer.getMaxCaptureRate()));
        audioOutput.setEnabled(true);
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
            halfHeight = container.getHeight() / 2;
            height = container.getHeight();
            width = container.getWidth();
            line = new WaveView(getActivity());
            final AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);

            timer = new Timer();

            task = new TimerTask() {

                synchronized public void run() {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //int volume_level=
                            double level = (float)(20 * Math.log10(mRecorder.getMaxAmplitude()/700.0)) ;

                            if(doneBuffering)
                            {
                                if(intensity == 0.103975)
                                {

                                }

                                if(intensity < 0.001)
                                {
                                    line.updateWaveWithLevel(0.1);
                                } else if(intensity < 0.5)
                                {
                                    line.updateWaveWithLevel(0.5);
                                } else
                                {
                                    line.updateWaveWithLevel(0.8);
                                }
                            } else
                            {
                                line.updateWaveWithLevel(0.01);
                            }
                        }
                    });
                }
            };

            timer.scheduleAtFixedRate(task, 0, 10);

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

                paint.setColor(Color.parseColor("#902E41"));

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

    private class CheckInternetTask extends AsyncTask<Void, Void, Boolean>
    {

        @Override
        protected Boolean doInBackground(Void... voids)
        {
            ConnectivityManager cm =
                    (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            return isConnected;
        }

        protected void onPostExecute(Boolean result) {
            if(!result)
            {
                Toast.makeText(getContext(), "You have no internet connection!", Toast.LENGTH_LONG).show();
            } else
            {
                player.prepareAsync();
            }
        }
    }

}
