package com.faizal.flickrimagesearch.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.faizal.flickrimagesearch.common.Common;
import com.faizal.flickrimagesearch.listeners.OnLoadMoreListener;
import com.faizal.flickrimagesearch.models.FlickerImageModel;
import com.faizal.flickrimagesearch.R;

import java.io.InputStream;
import java.util.ArrayList;

public class HomeGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<FlickerImageModel> mFlickerImg;
    private Context context;

    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;


    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private LruCache<String, Bitmap> mMemoryCache;

    public HomeGridAdapter(Context context, ArrayList<FlickerImageModel> flickerImg, RecyclerView mRecyclerView) {
        this.mFlickerImg = flickerImg;
        this.context = context;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mFlickerImg != null && mFlickerImg.size() >= 10) {
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            }
        });

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

    }

    public void updateDataSet(ArrayList<FlickerImageModel> flickerImg) {
        mFlickerImg = flickerImg;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_activity, parent, false);
            return new ItemsViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_loading_item, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

        if (viewHolder instanceof ItemsViewHolder) {
            if (mFlickerImg.get(i).getTitle().trim().length() > 0)
                ((ItemsViewHolder) viewHolder).tv_title.setText(mFlickerImg.get(i).getTitle());
            else
                ((ItemsViewHolder) viewHolder).tv_title.setText("Title not found");

            if (mFlickerImg.get(i).getImageUrl() != null)
                loadBitmap(mFlickerImg.get(i).getImageUrl(), ((ItemsViewHolder) viewHolder).img_flicker);
            else
                ((ItemsViewHolder) viewHolder).img_flicker.setImageResource(R.drawable.placeholder);

//            new DownloadImageTask(((ItemsViewHolder) viewHolder).img_flicker).execute(mFlickerImg.get(i).getImageUrl());


        } else if (viewHolder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) viewHolder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }


    public class ItemsViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_title;
        private ImageView img_flicker;

        public ItemsViewHolder(View view) {
            super(view);

            tv_title = view.findViewById(R.id.tv_title);
            img_flicker = view.findViewById(R.id.img_flicker);
        }
    }


    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mFlickerImg.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mFlickerImg == null ? 0 : mFlickerImg.size();
    }

    public void setLoaded() {
        isLoading = false;
    }


    //LRU cache
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        String urldisplay;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            try {
                urldisplay = urls[0];
                Bitmap bmp = null;
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    bmp = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
                return bmp;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                bmImage.setImageBitmap(result);
                addBitmapToMemoryCache(urldisplay, result);
            }
        }
    }


    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void loadBitmap(String resId, ImageView mImageView) {
        final String imageKey = String.valueOf(resId);

        final Bitmap bitmap = getBitmapFromMemCache(imageKey);
        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        } else {
            mImageView.setImageResource(R.drawable.placeholder);
//            BitmapWorkerTask task = new BitmapWorkerTask(mImageView);
//            task.execute(resId);
            Common common = new Common(context);
            if (common.isNetworkConnected()) {
                new DownloadImageTask(mImageView).execute(resId);
            }
        }
    }

}