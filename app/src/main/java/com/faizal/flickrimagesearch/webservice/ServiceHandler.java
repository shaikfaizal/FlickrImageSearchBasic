package com.faizal.flickrimagesearch.webservice;


import android.os.AsyncTask;

import com.faizal.flickrimagesearch.listeners.ResponseListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

//public class ServiceHandler {
public class ServiceHandler extends AsyncTask<String, Void, String> {
    String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;
    ResponseListener mResponseListener;
    Integer status_code;

    public ServiceHandler(ResponseListener responseListener) {
        this.mResponseListener = responseListener;
    }

    @Override
    protected String doInBackground(String... params) {
        String stringUrl = params[0];
        String result;
        String inputLine;
        REQUEST_METHOD = params[1];

        try {
            //Create a URL object holding our url
            URL myUrl = new URL(stringUrl);
            //Create a connection
            HttpsURLConnection connection = (HttpsURLConnection)
                    myUrl.openConnection();
            //Set methods and timeouts
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
//            connection.setRequestProperty("Content-type", "application/json");

            //Connect to our url
            connection.connect();
            int status = connection.getResponseCode();
            InputStreamReader streamReader = null;
            if (status < 400) {

                //Create a new InputStreamReader
                streamReader = new
                        InputStreamReader(connection.getInputStream());
            } else {
                streamReader = new InputStreamReader(connection.getErrorStream());
            }
            //Create a new buffered reader and String Builder
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();
            //Check if the line we are reading is not null
            while ((inputLine = reader.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            //Close our InputStream and Buffered reader
            reader.close();
            streamReader.close();
            //Set our result equal to our stringBuilder
            result = stringBuilder.toString();

            status_code = connection.getResponseCode();

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            result = null;
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (status_code == 200 && result != null)
            mResponseListener.OnSuccessResponseListener(status_code, result);
        else
            mResponseListener.OnErrorResponseListener(status_code, result);
    }
}