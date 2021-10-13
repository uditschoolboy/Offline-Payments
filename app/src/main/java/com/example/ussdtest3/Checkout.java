package com.example.ussdtest3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Checkout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
    }
    public void checkoutButtonClicked(View view) {
        Intent intent = new Intent(this, EntryScreen.class);
        intent.putExtra("usage", 1);
        startActivity(intent);
    }
}