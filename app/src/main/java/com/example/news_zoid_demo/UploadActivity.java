package com.example.news_zoid_demo;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.news_zoid_demo.activity.LoginActivity;
import com.example.news_zoid_demo.utils.HttpClient;
import com.example.news_zoid_demo.utils.NewszoidRestClient;
import com.google.android.gms.common.internal.Constants;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yayandroid.locationmanager.LocationManager;
import com.yayandroid.locationmanager.configuration.DefaultProviderConfiguration;
import com.yayandroid.locationmanager.configuration.GooglePlayServicesConfiguration;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.constants.ProviderType;
import com.yayandroid.locationmanager.listener.LocationListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static java.security.AccessController.getContext;

public class UploadActivity extends AppCompatActivity {
    private static final String TAG = UploadActivity.class.getSimpleName();
    private static final int REQUEST_VIDEO_CAPTURE = 300;
    private static final int READ_REQUEST_CODE = 200;
    private Uri uri;
    private String pathToStoredVideo;
    private String awsUrl;
    private String postLocation;
    private String title;
    private EditText titleEditText;
    private EditText selectCategory;
    private CircularProgressButton btn;
    private ProgressDialog dialog;
    private static AsyncHttpClient client = new AsyncHttpClient(8443,8443);
    private static String baseURL = "https://newszoid.stackroute.io";
    private static final String uploadEndPoint = "/content-service/api/v1/file/";
    private static final String postEndPoint = "/content-service/api/v1/post/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        Button captureVideoButton = (Button)findViewById(R.id.btn_capture);
        selectCategory = findViewById(R.id.category);
        titleEditText = findViewById(R.id.title);
        dialog = new ProgressDialog(UploadActivity.this);
        dialog.setMessage("Getting location..");
        btn = findViewById(R.id.btn_upload);

        //selectCategory.setEnabled(false);
        selectCategory.setFocusable(false);
        selectCategory.setInputType(0);


        dialog.show();

        postLocation = null;
        LocationConfiguration awesomeConfiguration = new LocationConfiguration.Builder()
                .keepTracking(false)
                .useGooglePlayServices(new GooglePlayServicesConfiguration.Builder()
                        .fallbackToDefault(true)
                        .askForGooglePlayServices(false)
                        .askForSettingsApi(true)
                        .failOnConnectionSuspended(true)
                        .failOnSettingsApiSuspended(false)
                        .ignoreLastKnowLocation(false)
                        .setWaitPeriod(20 * 1000)
                        .build())
                .useDefaultProviders(new DefaultProviderConfiguration.Builder()
                        .requiredTimeInterval(5 * 60 * 1000)
                        .requiredDistanceInterval(0)
                        .acceptableAccuracy(5.0f)
                        .acceptableTimePeriod(5 * 60 * 1000)
                        .gpsMessage("Turn on GPS?")
                        .setWaitPeriod(ProviderType.GPS, 20 * 1000)
                        .setWaitPeriod(ProviderType.NETWORK, 20 * 1000)
                        .build())
                .build();

        com.yayandroid.locationmanager.LocationManager awesomeLocationManager = new LocationManager.Builder(getApplicationContext())
                .activity(this)
                .configuration(awesomeConfiguration)
                .notify(new LocationListener() {
                    @Override
                    public void onProcessTypeChanged(int processType) {

                    }

                    @Override
                    public void onLocationChanged(Location location) {
                        dialog.dismiss();
                        System.out.println(location.getLatitude() + "," + location.getLongitude());
                        getData(location);
                        Geocoder geoCoder = new Geocoder(UploadActivity.this, Locale.getDefault()); //it is Geocoder
                        StringBuilder builder = new StringBuilder();
                        try {
                            List<Address> address = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            int maxLines = address.get(0).getMaxAddressLineIndex();
                            for (int i=0; i<maxLines; i++) {
                                String addressStr = address.get(0).getAddressLine(i);
                                System.out.println(addressStr);
                                builder.append(addressStr);
                                builder.append(" ");
                            }
                            System.out.println("qqqqqqqqq");
                            String fnialAddress = builder.toString(); //This is the complete address.
                            System.out.println(fnialAddress);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onLocationFailed(int type) {

                    }

                    @Override
                    public void onPermissionGranted(boolean alreadyHadPermission) {

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                })
                .build();

        awesomeLocationManager.get();

        selectCategory.setOnClickListener((View v)->{
            String[] singleChoiceItems = getResources().getStringArray(R.array.dialog_choice_category);
            int itemSelected = 0;
            new AlertDialog.Builder(UploadActivity.this)
                    .setTitle("Select category")
                    .setSingleChoiceItems(singleChoiceItems, itemSelected, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            selectCategory.setText(singleChoiceItems[i]);
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        captureVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent videoCaptureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if(videoCaptureIntent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(videoCaptureIntent, REQUEST_VIDEO_CAPTURE);
                }
            }
        });
        Button uploadVideoButton = (Button)findViewById(R.id.btn_upload);
        uploadVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //uploadVideoToServer(pathToStoredVideo);
                //dialog.show();
                //uploadFile(getAbsoluteUrl("/upload"), new File(pathToStoredVideo));
                btn.startAnimation();
                uploadFile(new File(pathToStoredVideo));
                String title = titleEditText.getText().toString();
                HttpClient httpClient = new HttpClient();
                Intent intent = getIntent();
                String jwtToken = intent.getStringExtra("jwtToken");
                String username = intent.getStringExtra("username");
                //JSONObject resp = httpClient.postNews(jwtToken, title, awsUrl, username, "Sports", postLocation);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_VIDEO_CAPTURE){
            uri = data.getData();
                pathToStoredVideo = getRealPathFromURIPath(uri, UploadActivity.this);
                Log.d(TAG, "Recorded Video Path " + pathToStoredVideo);

        }
    }

    private String getRealPathFromURIPath(Uri contentURI, Activity activity) {
        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }



    public void getData(Location location) {

        try {
            JsonParser parser_Json = new JsonParser();
            HttpClient httpClient = new HttpClient();
            JSONObject jsonObj = httpClient.getFromUrl("https://api.opencagedata.com/geocode/v1/json?q="+location.getLatitude()+"%2C"+location.getLongitude()+"&key=1deb99f114694de59f2b91b4279bc737&language=en&pretty=1");
            String Status = jsonObj.getString("status");
            JSONArray results = jsonObj.getJSONArray("results");
            JSONObject first = results.getJSONObject(0);
            //String city = first.getJSONObject("components").getString("city");
            String city = first.getString("formatted");
            System.out.println(city);
            postLocation = city;

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void uploadFile(File file) {
        Intent intent = getIntent();
        String jwtToken = intent.getStringExtra("jwtToken");
        RequestParams params = new RequestParams();
        try {
            params.put("file", file);
        } catch(FileNotFoundException e) {}
        NewszoidRestClient.post(this, uploadEndPoint, jwtToken, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println(response.toString());
            }

            @Override
            public void onSuccess(int k,Header[] headers, String response) {
                Log.d(TAG, "RRRRRR "+ response );
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) { }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if(statusCode == 201) {
                    awsUrl = responseString;
                    try {
                        postNews();
                    }
                    catch (Exception e) {}
                }
                Log.e("Login failure", statusCode+":"+responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), "Unexpected error please try again", Toast.LENGTH_SHORT).show();
                Log.e("Login failure", statusCode+":");
            }

        });
    }

    private void postNews() throws UnsupportedEncodingException, JSONException {
        Intent intent = getIntent();
        String jwtToken = intent.getStringExtra("jwtToken");
        String username = intent.getStringExtra("userName");
        String title = titleEditText.getText().toString();
        String category = selectCategory.getText().toString();

        Random rnd = new Random();
        int id = 100000 + rnd.nextInt(900000);

        JSONArray watchedBy = new JSONArray();
        JSONObject params = new JSONObject();
        params.put("title", title);
        params.put("id", id);
        params.put("videoUrl", awsUrl);
        params.put("location", postLocation);
        params.put("postedBy", username);
        params.put("category", category);
        params.put("watchedBy", watchedBy);
        StringEntity entity = new StringEntity(params.toString());
        NewszoidRestClient.post2(this, postEndPoint, jwtToken, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println(response.toString());
                Toast.makeText(getApplicationContext(), "Posted successfully!!", Toast.LENGTH_SHORT).show();
                Resources res = getApplicationContext().getResources();
                Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_done_white_48dp);
                btn.doneLoadingAnimation(0, bitmap);
                btn.revertAnimation();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) { }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), "Upload failed please try again", Toast.LENGTH_SHORT).show();
                Log.e("Upload failure", statusCode+":"+responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), "Unexpected error please try again", Toast.LENGTH_SHORT).show();
                Log.e("Upload failure", statusCode+":");
            }

        });
    }


}


