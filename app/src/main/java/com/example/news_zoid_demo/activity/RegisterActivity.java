package com.example.news_zoid_demo.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.news_zoid_demo.R;
import com.example.news_zoid_demo.utils.HttpClient;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

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
                    String date = DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.getTime());
                    SimpleDateFormat format = new SimpleDateFormat("MMM dd");
                    date = new SimpleDateFormat("yyyy-mm-dd").format(calendar.getTime());
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
            Log.w("dobbbbb", dob);
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
            HttpClient httpClient = new HttpClient();
            JSONObject resp = httpClient.register(username, password, email, name, dob, pref);
            Log.w("regActivity", resp.toString());
        });

    }



}
