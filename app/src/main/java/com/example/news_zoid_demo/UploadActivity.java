package com.example.news_zoid_demo;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import com.example.news_zoid_demo.ui.login.LoginActivity;
import com.example.news_zoid_demo.utils.HttpClient;
import com.google.android.gms.common.internal.Constants;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.security.AccessController.getContext;

public class UploadActivity extends AppCompatActivity {
    private static final String TAG = UploadActivity.class.getSimpleName();
    private static final int REQUEST_VIDEO_CAPTURE = 300;
    private static final int READ_REQUEST_CODE = 200;
    private Uri uri;
    private String pathToStoredVideo;
    private String awsUrl;
    private String postLocation;
    private static AsyncHttpClient client = new AsyncHttpClient(8443,8443);
    private static String baseURL = "https://newszoid.stackroute.io";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        Button captureVideoButton = (Button)findViewById(R.id.btn_capture);
        final EditText selectCategory = findViewById(R.id.category);
        final EditText titleEditText = findViewById(R.id.title);
        ProgressDialog dialog = new ProgressDialog(UploadActivity.this);
        dialog.setMessage("Your message..");

        selectCategory.setEnabled(false);

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
                    .setTitle("test3")
                    .setSingleChoiceItems(singleChoiceItems, itemSelected, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            selectCategory.setText(singleChoiceItems[i]);
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("cancel test", null)
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
                dialog.show();
                uploadFile(getAbsoluteUrl("/upload"), new File(pathToStoredVideo));
                String title = titleEditText.getText().toString();
                HttpClient httpClient = new HttpClient();
                Intent intent = getIntent();
                String jwtToken = intent.getStringExtra("jwtToken");
                JSONObject resp = httpClient.postNews(jwtToken, title, awsUrl, "j3rwin", "Sports", postLocation);
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

    private static String getAbsoluteUrl(String relativeUrl) {
        return baseURL + relativeUrl;
    }

    public Boolean uploadFile(String serverURL, File file){

        String upLoadServerUri = "https://newszoid.stackroute.io:8443/content-service/api/v1/file/";
        Intent intent = getIntent();
        String jwtToken = intent.getStringExtra("jwtToken");

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = file;
        //errMsg=Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!sourceFile.isFile())
        {
            Log.e("uploadFile", "Source File Does not exist");
            return false;
        }
        try {
            // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);
            conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            //conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("Authorization", "Bearer "+jwtToken);
            conn.setRequestProperty("file", file.getName());
            //conn.setRequestProperty("pid", "4");
            dos = new DataOutputStream(conn.getOutputStream());

            /*dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"title\""+ lineEnd);
            dos.writeBytes(lineEnd);
            EditText edit = (EditText)findViewById(R.id.editText);
            String title = edit.getText().toString();
            dos.writeBytes(title);
            dos.writeBytes(lineEnd);*/

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""+ file.getName() + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = new BufferedReader(new InputStreamReader(conn.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
            awsUrl = serverResponseMessage;
            Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
            if(serverResponseCode != 201)
            {
               Log.w("aaaaaaaaaa","errrrrrrrrrrrr");
            }

            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex) {
            // dialog.dismiss();
            ex.printStackTrace();
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
        } catch (Exception e) {
            //  dialog.dismiss();
            e.printStackTrace();
            Log.e("Upload file to server Exception", "Exception : " + e.getMessage(), e);
        }

        return true;
    }


    public void getData(Location location) {

        try {
            JsonParser parser_Json = new JsonParser();
            HttpClient httpClient = new HttpClient();
            JSONObject jsonObj = httpClient.getFromUrl("https://api.opencagedata.com/geocode/v1/json?q="+location.getLatitude()+"%2C"+location.getLongitude()+"&key=1deb99f114694de59f2b91b4279bc737&language=en&pretty=1");
            String Status = jsonObj.getString("status");
            JSONArray results = jsonObj.getJSONArray("results");
            JSONObject first = results.getJSONObject(0);
            String city = first.getJSONObject("components").getString("city");
            System.out.println(city);
            postLocation = city;

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }



}


