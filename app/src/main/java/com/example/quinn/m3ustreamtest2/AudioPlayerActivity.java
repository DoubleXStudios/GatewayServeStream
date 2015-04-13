package com.example.quinn.m3ustreamtest2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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
        System.out.println("WOOW ");
        return super.onCreateOptionsMenu(menu);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentIndex = 0;
        ConnectionTest x = new ConnectionTest(this);
        x.execute();
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

    }

    private void updateTextViews(){
        previousStationTextView.setText(getString(mStations[((mCurrentIndex-1) + mStations.length)% mStations.length].getResourceID()));
        currentStationTextView.setText(getString(mStations[mCurrentIndex].getResourceID()));
        nextStationTextView.setText(getString(mStations[(mCurrentIndex+1)% mStations.length].getResourceID()));
    }


    private void setPlayer(boolean needToStart){
        if (needToStart) {
                if(mPlaying){
                    setPlayer(false);
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



}