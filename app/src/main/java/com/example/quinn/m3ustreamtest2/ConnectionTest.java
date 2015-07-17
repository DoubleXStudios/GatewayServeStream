package com.example.quinn.m3ustreamtest2;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.Toast;

/**
 * Created by Kyle on 7/16/15.
 */
public class ConnectionTest extends AsyncTask<Void, Void, Boolean>
{
    private Context ctx;
    private AudioPlayerActivity passedActivity;
    private  boolean shouldStart;

    public ConnectionTest(Context ctx, AudioPlayerActivity activity, boolean shouldStart)
    {
        this.ctx = ctx;
        this.passedActivity = activity;
        this.shouldStart = shouldStart;
    }

    @Override
    protected Boolean doInBackground(Void... voids)
    {
        ConnectivityManager cm =
                (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    protected void onPostExecute(Boolean result) {
        if(!result)
        {
            Toast.makeText(ctx, "You have no internet connection!", Toast.LENGTH_SHORT).show();

            passedActivity.mStartStopButton.setBackgroundResource(R.drawable.play_red);
            passedActivity.doneBuffering = false;
            passedActivity.playPressed = false;
        } else
        {
            if(shouldStart)
            {
                passedActivity.player.prepareAsync();
            }
        }
    }
}
