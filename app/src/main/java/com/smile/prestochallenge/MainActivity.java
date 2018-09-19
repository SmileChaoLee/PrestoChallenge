package com.smile.prestochallenge;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.prestochallenge.Model.PhotoModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String flickr_api_key = "949e98778755d1982f537d56236bbb42";
    private static final String flickr_base_url = "https://api.flickr.com/services/rest/?api_key=" + flickr_api_key + "&format=json&nojsoncallback=1";
    private static final String flickr_base_search_url = flickr_base_url + "&method=flickr.photos.search&tags=mode";
    private static final String flickr_photo_detail_url = flickr_base_url + "&method=flickr.photos.getInfo&photo_id=";

    private ArrayList<Bitmap> imageBitmaps = new ArrayList();
    private ArrayList<String> imageSizes = new ArrayList();
    private ArrayList<String> imageDimensions = new ArrayList();
    private ArrayList<String> imageTitles = new ArrayList();
    private TextView loadingTextView;
    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        loadingTextView = findViewById(R.id.loadingTextView);
        loadingTextView.setVisibility(View.VISIBLE);

        listView = findViewById(R.id.imageListView);
        listView.setVisibility(View.GONE);

        Button quitButton = (Button)findViewById(R.id.quitButton);
        // okButton.setTextSize(textFontSize);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToPrevious();
            }
        });

        new FlickrAsyncTask().execute();
    }

    @Override
    public void onBackPressed() {
        returnToPrevious();
    }

    private void returnToPrevious() {
        finish();
    }

    private class myListAdapter extends ArrayAdapter {  // changed name to MyListAdapter from myListAdapter

        private int layoutId;
        private ArrayList<Bitmap> images;
        private ArrayList<String> sizes;
        private ArrayList<String> dimensions;
        private ArrayList<String> titles;

        @SuppressWarnings("unchecked")
        myListAdapter(Context context, int layoutId, ArrayList<Bitmap> images, ArrayList<String> sizes, ArrayList<String> dimensions, ArrayList<String> titles) {
            super(context, layoutId, titles);

            this.layoutId = layoutId;

            if (images == null) {
                this.images = new ArrayList<>();
            } else {
                this.images = images;
            }
            if (sizes == null) {
                this.sizes = new ArrayList<>();
            } else {
                this.sizes = sizes;
            }
            if (dimensions == null) {
                this.dimensions = new ArrayList<>();
            } else {
                this.dimensions = dimensions;
            }
            if (titles == null) {
                this.titles = new ArrayList<>();
            } else {
                this.titles = titles;
            }
        }

        @Nullable
        @Override
        public Object getItem(int position) {
            return super.getItem(position);
        }

        @SuppressWarnings("unchecked")
        @Override
        public int getPosition(@Nullable Object item) {
            return super.getPosition(item);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View view = getLayoutInflater().inflate(layoutId, parent,false);

            if (getCount() == 0) {
                return view;
            }

            int listViewHeight = parent.getHeight();
            int itemNum = 2;

            int itemHeight = listViewHeight / itemNum;    // items for one screen
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = itemHeight;

            // view.setLayoutParams(layoutParams);  // no needed

            ImageView showImageView = view.findViewById(R.id.showImageView);
            TextView imageSizeTextView = view.findViewById(R.id.imageSizeTextView);
            TextView imageDimensionTextView = view.findViewById(R.id.imageDimensionTextView);
            TextView imageTitleTextView = view.findViewById(R.id.imageTitleTextView);

            showImageView.setImageBitmap(images.get(position));
            imageSizeTextView.setText(sizes.get(position));
            imageDimensionTextView.setText(dimensions.get(position));
            imageTitleTextView.setText(titles.get(position));

            return view;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private class FlickrAsyncTask extends AsyncTask<Void, Void, Integer[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer[] doInBackground(Void... voids) {

            Integer result[] = new Integer[1];

            result[0] = Activity.RESULT_CANCELED;

            InputStream iStream = null;
            InputStreamReader iReader = null;
            HttpsURLConnection myConnection = null;
            String flickrData = new String("");

            try {
                // use REST APIs
                int perPage = 3;
                int pageNo = 1;
                URL searchUrl = new URL(getSearchUrl(perPage, pageNo));
                myConnection = (HttpsURLConnection)searchUrl.openConnection();
                myConnection.setReadTimeout(15000);
                myConnection.setConnectTimeout(15000);
                myConnection.setRequestMethod("GET");
                myConnection.setDoInput(true);
                // myConnection.setDoOutput(true);  // for write data to web. This method triggers POST request
                int responseCode = myConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {  // value = 200
                    // successfully
                    iStream = myConnection.getInputStream();
                    iReader = new InputStreamReader(iStream,"UTF-8");

                    StringBuilder sb = new StringBuilder("");
                    int readBuff = -1;
                    while((readBuff=iReader.read()) != -1) {
                        sb.append((char)readBuff);
                    }
                    flickrData = sb.toString();

                    try {
                        JSONObject jObject = new JSONObject(flickrData);
                        JSONObject photos = jObject.getJSONObject("photos");
                        JSONArray jPhotos = photos.getJSONArray("photo");

                        int photosSize = jPhotos.length();

                        JSONObject json;

                        PhotoModel photoModel;
                        ArrayList<PhotoModel> photoModels= new ArrayList<>();

                        for (int i=0; i<photosSize; i++) {
                            json = (JSONObject) jPhotos.get(i);
                            photoModel = new PhotoModel(json, flickr_photo_detail_url);
                            photoModels.add(photoModel);
                            imageBitmaps.add(photoModel.getPhotoBitmap());
                            imageSizes.add(String.valueOf(photoModel.getPhotoSize()) + " bytes");
                            imageDimensions.add(photoModel.getPhotoDimension());
                            imageTitles.add(photoModel.getPhotoTitle());
                        }
                        result[0] = Activity.RESULT_OK;

                    } catch (Exception e) {
                        Log.i(TAG, "Exception happened -> ");
                        e.printStackTrace();
                    }
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
            return result;
        }


        @Override
        protected void onPostExecute(Integer[] result) {
            super.onPostExecute(result);
            loadingTextView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            if (result != null) {
                if (result[0] == Activity.RESULT_OK) {
                    Toast.makeText(MainActivity.this,"Download succeeded.",Toast.LENGTH_LONG).show();
                    listView.setAdapter(new myListAdapter(MainActivity.this, R.layout.list_item, imageBitmaps, imageSizes, imageDimensions, imageTitles));
                    /*
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        }
                    });
                    */
                } else {
                    Toast.makeText(MainActivity.this,"Download failed.",Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this,"Download no result.",Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getSearchUrl(int perPage, int pageNo) {

        String sUrl = flickr_base_search_url + "&per_page=" + perPage + "&page=" + pageNo;
        return  sUrl;
    }
}
