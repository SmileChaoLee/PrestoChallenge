package com.smile.prestochallenge;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadIntentService extends IntentService {

    public static final String TAG = "DownloadIntentService";
    public static final String ActionName = "DownloadIntentService";

    private int result = Activity.RESULT_CANCELED;

    public DownloadIntentService() {
        super("DownloadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "onHandleIntent is called.");
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }

        String urlPath  = extras.getString("URL_PATH");

        result = Activity.RESULT_CANCELED;

        InputStream iStream = null;
        InputStreamReader iReader = null;
        HttpsURLConnection myConnection = null;
        String flickrData = new String("");

        try {
            // use REST APIs
            URL url = new URL(urlPath);
            myConnection = (HttpsURLConnection)url.openConnection();
            myConnection.setReadTimeout(15000);
            myConnection.setConnectTimeout(15000);
            myConnection.setRequestMethod("GET");
            myConnection.setDoInput(true);
            // myConnection.setDoOutput(true);  // for write data to web. This method triggers POST request
            int responseCode = myConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {  // value = 200
                // successfully
                Log.i(TAG, "REST Web Service -> Succeeded to connect.");

                iStream = myConnection.getInputStream();
                iReader = new InputStreamReader(iStream,"UTF-8");

                StringBuilder sb = new StringBuilder("");
                int readBuff = -1;
                while((readBuff=iReader.read()) != -1) {
                    sb.append((char)readBuff);
                }
                flickrData = sb.toString();

                result = Activity.RESULT_OK;  // successfully downloaded
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception.");
        } finally {
            if (iReader != null)
            {
                try {
                    iReader.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (iStream != null) {
                try {
                    iStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (myConnection != null) {
                // disconnect the resource
                myConnection.disconnect();
            }
        }

        // publish the result using sendBroadcast
        Intent notificationIntent = new Intent(ActionName);

        Bundle ex = new Bundle();
        ex.putInt("RESULT",result);
        ex.putString("FlickrData", flickrData);
        notificationIntent.putExtras(ex);

        sendBroadcast(notificationIntent); // this will work for global broadcast receiver

        System.out.println("Finished IntentService.");
    }
}
