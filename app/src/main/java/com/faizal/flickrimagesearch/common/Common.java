package com.faizal.flickrimagesearch.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Common {

    public static final String BASE_URL = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=3e7cc266ae2b0e0d78e279ce8e361736&\n" +
            "format=json&nojsoncallback=1&safe_search=1&per_page=20";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";
    public static final String SEARCH_PATTERN = "^[a-zA-Z ]+$";

    Context context;

    public Common(Context context) {
        this.context = context;
    }

    public static String getImageURL(Integer farm, String server, String id, String secret) {
        return "http://farm" + farm + ".static.flickr.com/" + server + "/" + id + "_" + secret + ".jpg";
    }

    public boolean isNetworkConnected() {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

    public static String getBaseUrl(Integer page, String searchText) {
        String url = BASE_URL;

        url = url + "&page=" + page + "&text=" + searchText + "";

        return url;
    }

}
