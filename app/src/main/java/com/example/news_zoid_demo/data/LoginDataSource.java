package com.example.news_zoid_demo.data;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.example.news_zoid_demo.data.model.LoggedInUser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource  extends Application {

    private static AsyncHttpClient client = new SyncHttpClient(32123);
    private static String baseURL = "http://172.23.239.59";
    static String jwtToken;

    public Result<LoggedInUser> login(String username, String password) {
        //String baseURL = LoginDataSource.getContext().getResources().getString(R.string.baseURL);
        //final String jwtToken;

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try {
                JSONObject params = new JSONObject();
                params.put("username", username);
                params.put("password", password);
                StringEntity entity = new StringEntity(params.toString());
                post(this, "/authenticate", entity, "application/json",
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                // Root JSON in response is an dictionary i.e { "data : [ ... ] }
                                // Handle resulting parsed JSON response here
                                try {
                                    jwtToken = response.getString("jwtToken");
                                }
                                catch (JSONException e){}
                                Log.w("successResp", response.toString());
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                                Log.e("errorResp", headers.toString());
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                                Log.e("errorResp", obj.toString());
                            }
                        });
            } catch (Exception e) {

            }
        }

        LoggedInUser user =
                new LoggedInUser(
                        username,
                        jwtToken);
        return new Result.Success<>(user);
    }


    public void logout() {
        // TODO: revoke authentication
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(Context context, String url, StringEntity params, String type, AsyncHttpResponseHandler responseHandler){
        client.post(context, getAbsoluteUrl(url), params, type, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return baseURL + relativeUrl;
    }
}
