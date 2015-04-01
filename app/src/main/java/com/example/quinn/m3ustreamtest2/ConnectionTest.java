package com.example.quinn.m3ustreamtest2;

/**
 * Created by andrew on 1/27/2015.
*/
/*
public class ConnectionTest extends AsyncTask<String,String,String> {
    private Context ctx;

    public ConnectionTest(Context ctx){
        this.ctx = ctx;
    }

    protected void onPreExecute(){
        Log.d("constat", "constat on preexecute");
    }

    protected String doInBackground(String... params) {

        String state = "";
        ConnectivityManager CManager =
                (ConnectivityManager) AudioPlayerActivity
                        .getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo NInfo = CManager.getActiveNetworkInfo();

        if (NInfo != null && NInfo.isConnectedOrConnecting()) {

            try {
                URL url = new URL("clients3.google.com/generate_204");   // Change to "http://google.com" for www  test.
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(10 * 1000);          // 10 s.
                urlc.connect();
                if (urlc.getResponseCode() == 204) {        // 200 = "OK" code (http connection is fine).
                    Log.wtf("Connection", "Success !");
                    return "true";
                } else {
                    return "false";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else Log.d("conStat","connection is wtf fix this here");

        return state;
    }
    protected void onPostExecute(String result){
        Log.d("conStat","constat says the connection state was " +result);
    }




}
*/