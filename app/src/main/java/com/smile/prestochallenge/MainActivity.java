package com.smile.prestochallenge;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ArrayList<Bitmap> imageBitmaps = new ArrayList();
    private ArrayList<String> imageSizes = new ArrayList();
    private ArrayList<String> imageDimensions = new ArrayList();
    private ArrayList<String> imageTitles = new ArrayList();
    private TextView loadingTextView;
    private ListView listView = null;

    private BroadcastReceiver receiver = null;

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

        receiver = new mainActivityReceiver();

        Intent intent = new Intent(MainActivity.this, DownloadIntentService.class);
        Bundle extras = new Bundle();
        // extras.putString("URL_PATH", "https://api.flickr.com/services/rest/?api_key=949e98778755d1982f537d56236bbb42&method=flickr.photos.search&format=json");
        // msg="Parameterless searches have been disabled. Please use flickr.photos.getRecent instead.
        extras.putString("URL_PATH", "https://api.flickr.com/services/rest/?api_key=949e98778755d1982f537d56236bbb42&method=flickr.photos.getRecent&format=json&nojsoncallback=1");
        intent.putExtras(extras);
        startService(intent);
        Log.d(TAG, "IntentService started.");
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
            int itemNum = 4;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                itemNum = 3;
            }
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
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadIntentService.ActionName);
        registerReceiver(receiver, filter);  // use global broadcast receiver
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);    // use global broadcast receiver
    }

    private class mainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context,Intent intent) {
            System.out.println("onReceive() is called.");
            loadingTextView.setVisibility(View.GONE);
            Bundle extras = null;
            String action = intent.getAction();
            switch (action) {
                case DownloadIntentService.ActionName:
                    extras = intent.getExtras();
                    if (extras == null) {
                        return;
                    }
                    loadingTextView.setVisibility(View.GONE);
                    int result = extras.getInt("RESULT");
                    String flickrData = extras.getString("FlickrData");
                    if (result == Activity.RESULT_OK) {
                        listView.setVisibility(View.VISIBLE);
                        try {
                            JSONObject jObject = new JSONObject(flickrData);
                            JSONObject photos = jObject.getJSONObject("photos");
                            JSONArray jPhotos = photos.getJSONArray("photo");
                            int photosSize = jPhotos.length();

                            imageBitmaps.clear();
                            imageSizes.clear();
                            imageDimensions.clear();
                            imageTitles.clear();

                            JSONObject json;
                            for (int i=0; i<photosSize; i++) {
                                json = (JSONObject) jPhotos.get(i);
                                imageBitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.smile));
                                imageSizes.add("0");
                                imageDimensions.add("10x10");
                                imageTitles.add(json.getString("title"));
                            }
                            Log.i(TAG, "Json succeeded -> ");
                        } catch(JSONException ex) {
                            Log.i(TAG, "Json failed -> ");
                            ex.printStackTrace();
                        }
                        listView.setAdapter(new myListAdapter(MainActivity.this, R.layout.list_item, imageBitmaps, imageSizes, imageDimensions, imageTitles));
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        });
                        Toast.makeText(context,"Download succeeded.",Toast.LENGTH_LONG).show();
                    } else {
                        // download failed
                        Toast.makeText(context,"Download failed.",Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
