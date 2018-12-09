package com.faizal.flickrimagesearch;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.faizal.flickrimagesearch.adapter.HomeGridAdapter;
import com.faizal.flickrimagesearch.common.Common;
import com.faizal.flickrimagesearch.dialog.SearchDialog;
import com.faizal.flickrimagesearch.listeners.OnLoadMoreListener;
import com.faizal.flickrimagesearch.listeners.ResponseListener;
import com.faizal.flickrimagesearch.listeners.SearchListener;
import com.faizal.flickrimagesearch.models.FlickerImageModel;
import com.faizal.flickrimagesearch.webservice.ServiceHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class HomeActivity extends AppCompatActivity implements ResponseListener, SearchListener {

    RecyclerView recyclerView;
    HomeGridAdapter adapter;
    RelativeLayout relative_home, relative_no_data;
    ArrayList<FlickerImageModel> flickerImage = new ArrayList<>();
    String objSearchText = "flower";
    private Integer current_page = 1;
    boolean isOnline = false;
    private LruCache<String, Bitmap> mLruCache;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initViews();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SearchDialog newFragment = SearchDialog.newInstance(1234);
                newFragment.setSearchlistener(HomeActivity.this);
                newFragment.show(getSupportFragmentManager(), "dialog");

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void initViews() {

        relative_home = findViewById(R.id.relative_home);
        relative_no_data = findViewById(R.id.relative_no_data);
        recyclerView = (RecyclerView) findViewById(R.id.card_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new HomeGridAdapter(getApplicationContext(), flickerImage, recyclerView);
        recyclerView.setAdapter(adapter);

        relative_home.setVisibility(View.VISIBLE);
        relative_no_data.setVisibility(View.GONE);

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (isOnline) {
//                            Log.e("haint", "Load More in online");
                            return;
                        }
                        if (flickerImage != null && flickerImage.size() >= 10) {
                            flickerImage.add(null);
                            adapter.updateDataSet(flickerImage);
                            adapter.notifyDataSetChanged();

                            current_page += 1;
                            initnetworkcall();
                        } else {
                            adapter.setLoaded();
                        }
                    }
                });
            }
        });

        relative_no_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initnetworkcall();
            }
        });

        initnetworkcall();

    }

    private void initnetworkcall() {
        Common common = new Common(getApplicationContext());
        if (!common.isNetworkConnected()) {
            if (flickerImage.size() <= 0) {
                relative_no_data.setVisibility(View.VISIBLE);
            }
            relative_home.setVisibility(View.GONE);
            Snackbar.make(findViewById(R.id.home_coordinatelayout), "Please check internet setting", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            removeLoader();
            return;
        }
        isOnline = true;
        new ServiceHandler(this).execute(Common.getBaseUrl(current_page, objSearchText), Common.METHOD_POST);
    }

    private void prepareData(String result) {

        try {
            JSONObject root = new JSONObject(result);
            if (root.has("photos")) {
                JSONObject obj_photos = root.getJSONObject("photos");    //which contains basic data

                String total_img = obj_photos.getString("total");

                if (obj_photos.has("photo")) {
                    JSONArray array_photo = obj_photos.getJSONArray("photo"); // contains photo with details


                    for (int i = 0; i < array_photo.length(); i++) {
                        FlickerImageModel imageModel = new FlickerImageModel();

                        JSONObject object = array_photo.getJSONObject(i);

                        imageModel.setId(object.getString("id"));
                        imageModel.setOwner(object.getString("owner"));
                        imageModel.setSecret(object.getString("secret"));
                        imageModel.setServer(object.getString("server"));
                        imageModel.setFarm(object.getInt("farm"));
                        imageModel.setTitle(object.getString("title"));
                        imageModel.setIspublic(object.getInt("ispublic"));
                        imageModel.setIsfriend(object.getInt("isfriend"));
                        imageModel.setIsfamily(object.getInt("isfamily"));
                        imageModel.setImageUrl(Common.getImageURL(
                                imageModel.getFarm(),
                                imageModel.getServer(),
                                imageModel.getId(),
                                imageModel.getSecret())
                        );

                        flickerImage.add(imageModel);
                    }
                }

                relative_home.setVisibility(View.GONE);
                adapter.updateDataSet(flickerImage);
                adapter.notifyDataSetChanged();

                Snackbar.make(findViewById(R.id.home_coordinatelayout),
                        objSearchText + " : " + flickerImage.size() + " / " + total_img,
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            if (flickerImage.size() <= 0) {
                relative_no_data.setVisibility(View.VISIBLE);
                Snackbar.make(findViewById(R.id.home_coordinatelayout), "No data available", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                relative_no_data.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            relative_home.setVisibility(View.GONE);

            if (flickerImage.size() <= 0) {
                relative_no_data.setVisibility(View.VISIBLE);
            }
            Snackbar.make(findViewById(R.id.home_coordinatelayout), "Exception occured", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
//        return flickerImage;
    }

    @Override
    public void OnSuccessResponseListener(int responseCode, String response) {
        isOnline = false;
        removeLoader();
        prepareData(response);
    }

    @Override
    public void OnErrorResponseListener(int responseCode, String response) {
        Log.e("Failure", "response: " + response);

        isOnline = false;
        removeLoader();

        if (flickerImage.size() <= 0) {
            relative_no_data.setVisibility(View.VISIBLE);
        }
        relative_home.setVisibility(View.GONE);
        Snackbar.make(findViewById(R.id.home_coordinatelayout), "Failure: " + responseCode, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }

    private void removeLoader() {
        if (flickerImage.size() > 10) {
            flickerImage.remove(flickerImage.size() - 1);
            adapter.notifyItemRemoved(flickerImage.size());
            adapter.setLoaded();
        }
    }

    @Override
    public void OnSearchComplete(String searchText) {
        objSearchText = searchText;
        reset();
    }

    private void reset() {
        relative_home.setVisibility(View.VISIBLE);
        current_page = 1;
        flickerImage = new ArrayList<>();
        initnetworkcall();
    }
}
