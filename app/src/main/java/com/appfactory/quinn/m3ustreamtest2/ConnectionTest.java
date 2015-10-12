package com.appfactory.quinn.m3ustreamtest2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import com.github.gfranks.minimal.notification.GFMinimalNotification;
import com.github.gfranks.minimal.notification.GFMinimalNotificationStyle;

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

            passedActivity.doneBuffering = false;
            passedActivity.internetErrorOccured = true;

        } else
        {
            if(shouldStart)
            {
                passedActivity.player.prepareAsync();
            } else
            {
                if(passedActivity.internetErrorOccured)
                {
                    passedActivity.internetErrorOccured = false;
                    passedActivity.setupPlayer();
                    passedActivity.player.prepareAsync();

                    if(passedActivity.notification != null)
                    {
                        passedActivity.notification.dismiss();
                    }
                    passedActivity.notification = new GFMinimalNotification(passedActivity, GFMinimalNotificationStyle.WARNING , "", "Your stream is loading....",
                            0);
                    passedActivity.notification.show(passedActivity);
                }
            }
        }
    }
}
