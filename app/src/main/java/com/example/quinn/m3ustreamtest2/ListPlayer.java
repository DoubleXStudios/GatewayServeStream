package com.example.quinn.m3ustreamtest2;

/**
 * Created by Quinn on 3/31/15.
 */

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

public class ListPlayer implements Runnable {
    private String [] uris;
    private AudioPlayerActivity activity;
    private MediaPlayer player;
    private boolean keepPlaying = true;

    public ListPlayer(AudioPlayerActivity activity, String[] uris) {
        this.uris = uris;
        this.activity = activity;
        activity.mListPlayer = this;
    }

    public void run() {
        for (String uri:uris) {
            if (!keepPlaying) {
                break;
            }
            System.out.println("About to play " + uri);
            play1(uri);
            synchronized(this) {
                try {
                    this.wait();
                } catch (Exception e) {
                    System.out.println("play failed " + e.toString());
                }
            }
        }
    }

    private void play1(String uriStr) {

        try {
            // Try the URL directly (ok for Android 3.0 upwards)
            tryMediaPlayer(uriStr);
        } catch(Exception e) {
            // Try downloading the file and then playing it - needed for Android 2.2
            try {
                downloadToLocalFile(uriStr, "audiofile.ogg");
                File localFile = activity.getFileStreamPath("audiofile.ogg");
                tryMediaPlayer(localFile.getAbsolutePath());
            } catch(Exception e2) {
                System.out.println("File error " + e2.toString());
            }
        }
    }

    private void downloadToLocalFile(String uriStr, String filename) throws Exception {
        URL url = new URL(Uri.encode(uriStr, ":/"));
        BufferedInputStream reader =
                new BufferedInputStream(url.openStream());

        File f = new File("audiofile.ogg");
        FileOutputStream fOut = activity.openFileOutput("audiofile.ogg",
                Context.MODE_WORLD_READABLE);
        BufferedOutputStream writer = new BufferedOutputStream(fOut);

        byte[] buff = new byte[1024];
        int nread;
        System.out.println("Downloading");
        while ((nread = reader.read(buff, 0, 1024)) != -1) {
            writer.write(buff, 0, nread);
        }
        writer.close();
    }

    private void tryMediaPlayer(String uriStr) throws Exception {
        player = new MediaPlayer();
        player.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer player) {
                synchronized(ListPlayer.this) {
                    ListPlayer.this.notifyAll();
                }
            }
        });
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDataSource(uriStr);
        player.prepare();
        player.start();
    }

    public void stopPlaying() {
        keepPlaying = false;
        player.stop();
        //player.release();
        synchronized(this) {
            notifyAll();
        }
    }

}
