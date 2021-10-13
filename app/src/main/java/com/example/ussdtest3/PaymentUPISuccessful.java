package com.example.ussdtest3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

public class PaymentUPISuccessful extends AppCompatActivity {
    private TextView txt_upi_id,txt_reference_id,txt_amount,txt_user_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_u_p_i_successful);
        String upi_id = "5432@apl";
        String amount = "12.34";
        String reference_id = "123432";
        String user_name="Sarthak Jain";

        Intent intent = getIntent();

        if(intent.hasExtra("upi_id"))
            upi_id = intent.getStringExtra("upi_id").toString();
        if(intent.hasExtra("amount"))
            amount = intent.getStringExtra("amount").toString();
        if(intent.hasExtra("reference_id"))
            reference_id = intent.getStringExtra("reference_id").toString();
        if(intent.hasExtra("user_name"))
            user_name = intent.getStringExtra("user_name").toString();

        //changing amount format
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        String moneyString = formatter.format(Double.valueOf(amount));


        txt_upi_id=findViewById(R.id.txt_upi_id);
        txt_user_name = findViewById(R.id.txt_user_name);
        txt_reference_id=findViewById(R.id.txt_reference_id);
        txt_amount=findViewById(R.id.txt_amount);


        txt_amount.setText(moneyString);
        txt_upi_id.setText(upi_id);
        txt_user_name.setText("Paid to "+user_name);
        txt_reference_id.setText("Reference Id is "+reference_id);
    }

    public void redirectToEntryScreen(View view) {
        Intent intent = new Intent(this, EntryScreen.class);
        startActivity(intent);
    }
}