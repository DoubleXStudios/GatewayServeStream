package com.example.quinn.m3ustreamtest2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class AudioPlayerActivity extends Activity {
    public static Context context;
    public static AudioPlayerActivity activity;

    public ListPlayer listPlayer;
    private RadioGroup stationsGroup;
    private RadioButton defaultRadioButton;
    private RadioButton jazzRadioButton;
    private RadioButton readingServiceRadioButton;
    private Button stopButton;
    private Button startButton;
    private ImageView image;
    private boolean started;
    private String currentSource;

    //public static Context getInstance() {}



    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        started = false;
        setContentView(R.layout.activity_audio_player);
        defaultRadioButton = (RadioButton) findViewById(R.id.radio91point1);
        jazzRadioButton = (RadioButton) findViewById(R.id.radioJazz);
        readingServiceRadioButton = (RadioButton) findViewById(R.id.reading_service_radio_button);
        stationsGroup = (RadioGroup) findViewById(R.id.stationSelectionGroup);
        stopButton = (Button) findViewById(R.id.stop_button);
        startButton = (Button) findViewById(R.id.play_button);
        currentSource = "";




        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                listPlayer.stopPlaying();
                started = false;
            }
        });
        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(!started){
                    startPlayer(currentSource = getSource());
                    started = true;
                } else {
                    if (currentSource != getSource()){
                        listPlayer.stopPlaying();
                        startPlayer(currentSource = getSource());
                        started = true;
                    }
                }
            }
        });

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
    private String getSource (){
        if(defaultRadioButton.isChecked()){
            return "http://media.gtc.edu:8000/stream.m3u";
        } else if(jazzRadioButton.isChecked()){
            return "http://199.255.3.11:88/broadwave.m3u?src=1&rate=1";
        } else if(readingServiceRadioButton.isChecked()){
            return "http://199.255.3.11:88/broadwave.m3u?src=4&rate=1";
        } else {
            return "http://sportsweb.gtc.edu:8000/Sportsweb.m3u";
        }
    }


    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        System.out.println("External Player finished");
        synchronized(listPlayer) {
            listPlayer.notifyAll();
        }
    }
    public Context getContext(){
        return this.getBaseContext();
    }

}