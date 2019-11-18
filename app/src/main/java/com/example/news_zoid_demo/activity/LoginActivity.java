package com.example.news_zoid_demo.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.news_zoid_demo.R;
import com.example.news_zoid_demo.UploadActivity;
import com.example.news_zoid_demo.utils.NewszoidRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class LoginActivity extends AppCompatActivity {

    private static final String loginEndPoint = "/login-service/api/v1/authenticate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.btn_login);
        final Button registerButton = findViewById(R.id.btn_register);

        loginButton.setOnClickListener((View v)->{
                Log.w("clickBTN", usernameEditText.getText().toString());
                try {
                    login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                catch (Exception e) {

                }
        });

        registerButton.setOnClickListener((View v)-> {
            Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            this.startActivity(registerIntent);
        });
    }


    private void login(String username, String password) throws JSONException, UnsupportedEncodingException {
        CircularProgressButton btn = findViewById(R.id.btn_login);
        btn.startAnimation();
        JSONObject params = new JSONObject();
        params.put("username", username);
        params.put("password", password);
        StringEntity param = new StringEntity(params.toString());
        NewszoidRestClient.post(this, loginEndPoint, param, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Resources res = getApplicationContext().getResources();
                Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_done_white_48dp);
                btn.doneLoadingAnimation(0, bitmap);
                System.out.println(response.toString());
                Toast.makeText(getApplicationContext(), "LoggedIn successfully!!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, UploadActivity.class);
                String jwt = null;
                try {
                    jwt = response.getString("jwtToken");
                }
                catch (JSONException e) {
                    return;
                }
                intent.putExtra("jwtToken", jwt);
                intent.putExtra("userName", username);
                startActivity(intent);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) { }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if(statusCode == 403) {
                    Toast.makeText(getApplicationContext(), "Invalid username/password", Toast.LENGTH_SHORT).show();
                }
                btn.revertAnimation();
                Log.e("Login failure", responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if(statusCode == 403) {
                    Toast.makeText(getApplicationContext(), "Invalid username/password", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected error please try again", Toast.LENGTH_SHORT).show();
                }
                btn.revertAnimation();
                Log.e("Login failure", statusCode+":");
            }

        });
    }
}
