package com.faizal.flickrimagesearch.listeners;

public interface ResponseListener {
    void OnSuccessResponseListener(int responseCode, String response);
    void OnErrorResponseListener(int responseCode, String response);

}
