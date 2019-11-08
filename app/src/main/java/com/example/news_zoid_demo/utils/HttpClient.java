package com.example.news_zoid_demo.utils;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.example.news_zoid_demo.data.Result;
import com.example.news_zoid_demo.data.model.LoggedInUser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class HttpClient extends Application {
    private static AsyncHttpClient client = new SyncHttpClient(8443,8443);
    private static final String baseURL = "https://newszoid.stackroute.io";
    static String jwtToken;
    static JSONObject retResp;

    public JSONObject register(String username, String password, String email, String name, String dob, List<String> pref) {
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
                params.put("email", email);
                params.put("name", name);
                params.put("newsPreferences", new JSONArray(pref));
                params.put("dateOfBirth", dob);
                StringEntity entity = new StringEntity(params.toString());
                post(this, "/registration-service/api/v1/register", entity, "application/json",
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    retResp = response;
                                    jwtToken = response.getString("jwtToken");
                                }
                                catch (JSONException e){}
                                Log.w("successResp", response.toString());
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                                retResp = null;
                                Log.e("errorResp", res);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                                retResp = null;
                                Log.e("errorResp", obj.toString());
                            }
                        });
            } catch (Exception e) {

            }
        }

        return retResp;
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
