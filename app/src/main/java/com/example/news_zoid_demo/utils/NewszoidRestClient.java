package com.example.news_zoid_demo.utils;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.entity.StringEntity;

public class NewszoidRestClient {
    private static final String BASE_URL = "https://newszoid.stackroute.io";

    private static AsyncHttpClient client = new AsyncHttpClient(8443,8443);

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(Context context, String url, String jwt, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", "Bearer "+jwt);
        client.post(context, getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post2(Context context, String url, String jwt, StringEntity params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", "Bearer "+jwt);
        client.post(context, getAbsoluteUrl(url), params, "application/json",responseHandler);
    }

    public static void get(Context context, String url, StringEntity params, AsyncHttpResponseHandler responseHandler) {
        client.get(context, getAbsoluteUrl(url), params, "application/json", responseHandler);
    }

    public static void post(Context context,String url, StringEntity params, AsyncHttpResponseHandler responseHandler) {
        client.post(context, getAbsoluteUrl(url), params, "application/json", responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
