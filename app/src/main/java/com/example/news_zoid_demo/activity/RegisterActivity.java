package com.example.news_zoid_demo.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.news_zoid_demo.R;
import com.example.news_zoid_demo.utils.HttpClient;
import com.example.news_zoid_demo.utils.NewszoidRestClient;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class RegisterActivity extends AppCompatActivity {

    private static final String registerEndPoint = "/registration-service/api/v1/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final Calendar calendar = Calendar.getInstance();
        EditText birthDay = findViewById(R.id.input_dob);
        final Button registerBtn = findViewById(R.id.btn_register);
        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final EditText emailEditText = findViewById(R.id.email);
        final EditText nameEditText = findViewById(R.id.name);
        final EditText dobEditText = findViewById(R.id.input_dob);

        birthDay.setOnClickListener((View v)->{
            DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String date = new SimpleDateFormat("yyyy-mm-dd").format(calendar.getTime());
                    birthDay.setText(date);
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        registerBtn.setOnClickListener((View v)->{
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String name = nameEditText.getText().toString();
            String dob = dobEditText.getText().toString();
            List<String> pref = new ArrayList<>();
            ChipGroup chg = findViewById(R.id.chipGroup);
            int chipsCount = chg.getChildCount();
            if (chipsCount == 0) {
            } else {
                int i = 0;
                while (i < chipsCount) {
                    Chip chip = (Chip) chg.getChildAt(i);
                    if (chip.isChecked() ) {
                        String msg = chip.getText().toString();
                        pref.add(msg);
                    }
                    i++;
                };
            }
            //HttpClient httpClient = new HttpClient();
            //JSONObject resp = httpClient.register(username, password, email, name, dob, pref);
            //Log.w("regActivity", resp.toString());
            try {
                register(username, password, email, name, dob, pref);
            }
            catch (JSONException | UnsupportedEncodingException e) {}
        });

    }


    private void register(String username, String password, String email, String name, String dob, List<String> pref) throws JSONException, UnsupportedEncodingException {
        CircularProgressButton btn = findViewById(R.id.btn_register);
        btn.startAnimation();
        JSONObject params = new JSONObject();
        params.put("username", username);
        params.put("password", password);
        params.put("email", email);
        params.put("name", name);
        params.put("newsPreferences", new JSONArray(pref));
        params.put("dateOfBirth", dob);
        StringEntity param = new StringEntity(params.toString());
        NewszoidRestClient.post(this, registerEndPoint, param, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Resources res = getApplicationContext().getResources();
                Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_done_white_48dp);
                btn.doneLoadingAnimation(0, bitmap);
                System.out.println(response.toString());
                Toast.makeText(getApplicationContext(), "Registered successfully!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) { }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if(statusCode == 409) {
                    Toast.makeText(getApplicationContext(), "User already exist!!", Toast.LENGTH_SHORT).show();
                }
                btn.revertAnimation();
                Log.e("Register failure", responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), "Unexpected error please try again", Toast.LENGTH_SHORT).show();
                Log.e("Register failure", statusCode+":");
            }

        });
    }



}
