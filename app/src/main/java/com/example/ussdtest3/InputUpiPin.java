package com.example.ussdtest3;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.StringTokenizer;

public class InputUpiPin extends AppCompatActivity {

    private Intent intent;
    private EditText txt_upi_pin;
    private String upi_pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_upi_pin);
        txt_upi_pin = findViewById(R.id.txt_upi_pin);
        txt_upi_pin.performClick();
    }

    public void submitUpiPin(View v) {
        upi_pin = txt_upi_pin.getText().toString();
        if (upi_pin.length() == 0) {
            Toast.makeText(this, "UPI Pin is empty!", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent();
        Intent current = getIntent();
        if (current != null && current.hasExtra("CALLBACK_CODE")) {
            System.out.println("My callback code: " + current.getStringExtra("CALLBACK_CODE"));
            intent.putExtra("CALLBACK_CODE", current.getStringExtra("CALLBACK_CODE").toString());
        }
        intent.putExtra("upi_pin", upi_pin);
        setResult(RESULT_OK, intent);
        finish();
    }
}