package com.example.ussdtest3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class E1Sc extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e1_sc);
    }
    public void localMerchantsButtonClicked(View view) {
        Intent intent = new Intent(this, EntryScreen.class);
        intent.putExtra("usage", 0);
        startActivity(intent);
    }
    public void buttonClicked(View view) {
        if(view.getId() == R.id.buttononline) {
            Intent intent = new Intent(this, Checkout.class);
            startActivity(intent);
            return;
        }
        Intent intent = new Intent(this, EntryScreen.class);
        intent.putExtra("usage", 0);
        startActivity(intent);
    }
}