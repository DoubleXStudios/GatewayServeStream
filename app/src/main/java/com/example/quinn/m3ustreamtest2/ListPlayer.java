package com.example.quinn.m3ustreamtest2;

/**
 * Created by Quinn on 3/31/15.
 */

import android.media.MediaPlayer;

import java.io.IOException;

public class ListPlayer {
    private String [] uris;
    private AudioPlayerActivity activity;
    private MediaPlayer player;
    private boolean keepPlaying = true;

    public ListPlayer() {

    }


    public void play()
    {
        player = new MediaPlayer();
        try
        {
            player.setDataSource("http://media.gtc.edu:8000/stream\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                player.start();
            }
        });
        player.prepareAsync();
    }

    public void stop()
    {
        System.out.println("Player Stopper");
        keepPlaying = false;
        player.stop();
        synchronized(this) {
            notifyAll();
        }
    }

}
