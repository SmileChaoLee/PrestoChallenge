package com.smile.prestochallenge.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.smile.prestochallenge.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class PhotoModel {

    private Bitmap photoBitmap;
    private int photoSize;
    private String photoDimension;
    private String photoTitle;

    private String id;
    private int farm;
    private String server;
    private String secret;
    private String originalFormat;

    private JSONObject jPhotoInfoObject;

    private String photoUrl;
    private String flickr_photo_detail_url;

    public PhotoModel(JSONObject json, String flickr_photo_detail_url) {
        try {
            photoTitle = json.getString("title");
            id = json.getString("id");
            farm = json.getInt("farm");
            server = json.getString("server");
            secret = json.getString("secret");
            this.flickr_photo_detail_url = flickr_photo_detail_url;
            getPhotoInfo();
            JSONObject jObj = jPhotoInfoObject.getJSONObject("photo");
            originalFormat = jObj.getString("originalformat");
            photoUrl = "https://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret + "." + originalFormat;
            photoBitmap = getPhoto();
            if (photoBitmap != null) {
                photoSize = photoBitmap.getByteCount();
                int width = photoBitmap.getWidth();
                int height = photoBitmap.getHeight();
                photoDimension = String.valueOf(width) + "x" + String.valueOf(height);
            } else {
                photoSize = 0;
                photoDimension = "";
            }


        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public Bitmap getPhotoBitmap() {
        return photoBitmap;
    }
    public int getPhotoSize() {
        return photoSize;
    }
    public String getPhotoDimension() {
        return photoDimension;
    }
    public String getPhotoTitle() {
        return photoTitle;
    }
    public String getId() {
        return id;
    }
    public int getFarm() {
        return farm;
    }
    public String getServer() {
        return server;
    }
    public String getSecret() {
        return secret;
    }
    public String getPhotoUrl() {
        return photoUrl;
    }

    private void getPhotoInfo() {
        // https://api.flickr.com/services/rest/?api_key=949e98778755d1982f537d56236bbb42&format=json&nojsoncallback=1&method=flickr.photos.getInfo&photo_id=42972565000

        jPhotoInfoObject = new JSONObject();

        InputStream iStream = null;
        InputStreamReader iReader = null;
        HttpsURLConnection myConnection = null;
        String flickrData = new String("");
        try {
            URL photoInfoUrl = new URL(flickr_photo_detail_url + id);
            myConnection = (HttpsURLConnection) photoInfoUrl.openConnection();
            myConnection.setReadTimeout(15000);
            myConnection.setConnectTimeout(15000);
            myConnection.setRequestMethod("GET");
            myConnection.setDoInput(true);
            int responseCode = myConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {  // value = 200
                // successfully
                iStream = myConnection.getInputStream();
                iReader = new InputStreamReader(iStream, "UTF-8");

                StringBuilder sBuild = new StringBuilder("");
                int readB = -1;
                while ((readB = iReader.read()) != -1) {
                    sBuild.append((char) readB);
                }
                flickrData = sBuild.toString();

                jPhotoInfoObject = new JSONObject(flickrData);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
    }

    private Bitmap getPhoto() {
        InputStream iStream = null;
        HttpsURLConnection myConnection = null;
        Bitmap bmp = null;
        try {
            URL photoInfoUrl = new URL(photoUrl);
            myConnection = (HttpsURLConnection) photoInfoUrl.openConnection();
            myConnection.setReadTimeout(15000);
            myConnection.setConnectTimeout(15000);
            myConnection.setRequestMethod("GET");
            myConnection.setDoInput(true);
            int responseCode = myConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {  // value = 200
                // successfully
                iStream = myConnection.getInputStream();
                bmp = BitmapFactory.decodeStream(iStream);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
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

        return bmp;
    }
}
