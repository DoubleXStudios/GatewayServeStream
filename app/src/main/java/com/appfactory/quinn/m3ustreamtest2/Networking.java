package com.appfactory.quinn.m3ustreamtest2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kylez on 9/18/15.
 */
public class Networking
{

    public static String getStoredApiKey(AudioPlayerActivity context) {
        SharedPreferences pref = context.getPreferences(Context.MODE_PRIVATE);
        String key = pref.getString("ApiKey", "");

        if (!key.equals("")) {
            return key;
        }

        return null;
    }

    public static boolean checkForInternet(AudioPlayerActivity ctx)
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static class GetBannerAds extends AsyncTask<Void, Void, Void> {
        String baseUrl = "http://appfactoryuwp.com/imageserver";
        AudioPlayerActivity mainActivity;
        ArrayList<String> imageUrls = new ArrayList<>();
        ArrayList<Bitmap> imageBitmaps = new ArrayList<>();

        public GetBannerAds(AudioPlayerActivity context) {
            mainActivity = context;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if(checkForInternet(mainActivity))
            {
                String apiKey = getStoredApiKey(mainActivity);

                if (apiKey == null) {
                    apiKey = obtainApiKey();
                    storeApiKey(apiKey);
                }

                String imageAlbumXml = getAlbumXml(apiKey);

                Pattern p = Pattern.compile("imagefilename");
                Matcher m = p.matcher(imageAlbumXml);
                int count = 0;
                while (m.find())
                {
                    count +=1;
                }

                for(int i=0; i<count/2;i++)
                {
                    String split = imageAlbumXml.split("<"+"imagefilename"+">")[1+i].split("</"+"imagefilename"+">")[0];
                    imageUrls.add(split);
                }

                for(String imageUrl: imageUrls)
                {
                    Bitmap bitmap = getImageBitmap(imageUrl);
                    imageBitmaps.add(bitmap);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mainActivity.bannersAds = imageBitmaps;
        }

        private Bitmap getImageBitmap(String imageUrl)
        {
            URL url;
            HttpURLConnection urlConnection = null;
            Bitmap bitmap = null;

            try {
                url = new URL(imageUrl);

                urlConnection = (HttpURLConnection) url
                        .openConnection();

                InputStream bits = urlConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(bits);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace(); //If you want further info on failure...
                }
            }

            return bitmap;
        }

        private String obtainApiKey() {
            String serverXml = "";
            String apiKey;
            URL url;
            HttpURLConnection urlConnection = null;
            String urlString = baseUrl + "/api/yum/key/104";
            try {
                url = new URL(urlString);

                urlConnection = (HttpURLConnection) url
                        .openConnection();
                urlConnection.addRequestProperty("X-API-KEY", "b5d4af3cfb232c01311b183d42d05648");
                urlConnection.addRequestProperty("X-SHHH-ITS-A-SECRET", "73509e2f8981fd1247f400de53c60b0f8053fbb5");

                InputStream in = urlConnection.getInputStream();

                InputStreamReader isw = new InputStreamReader(in);

                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    serverXml += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace(); //If you want further info on failure...
                }
            }

            apiKey = parseApiKeyRequestOutput(serverXml);

            return apiKey;
        }

        private String parseApiKeyRequestOutput(String output) {
            String apiKey = output.substring(output.indexOf("<item>") + 6, output.indexOf("</item>"));

            return apiKey;
        }

        private void storeApiKey(String key) {
            SharedPreferences.Editor editor = mainActivity.getPreferences(Context.MODE_PRIVATE).edit();

            editor.putString("ApiKey", key);
            editor.commit();
        }

        private String getAlbumXml(String apiKey) {
            URL url;
            HttpURLConnection urlConnection = null;
            String urlString = baseUrl + "/api/apt";
            String serverXml = "";

            try {
                url = new URL(urlString);

                urlConnection = (HttpURLConnection) url
                        .openConnection();
                urlConnection.addRequestProperty("X-API-KEY", apiKey);

                InputStream in = urlConnection.getInputStream();

                InputStreamReader isw = new InputStreamReader(in);

                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    serverXml += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return serverXml;
            }
        }
    }

}
