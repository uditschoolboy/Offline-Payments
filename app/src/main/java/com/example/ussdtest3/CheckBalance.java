package com.example.ussdtest3;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

public class CheckBalance extends AppCompatActivity implements ServiceCallbacks{

    /*************************************/
    //Variables for storing the upi pin, It will be fetched on the another activity
    private EditText editTextUpiPin, editTextUpiId, editTextAmount, editTextRemarks;
    private RadioButton radioButton;
    public static TextView txtViewStatus;
    private String upi_pin;
    private int simCardChoosen;
    /*************************************/

    private Intent ussdService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_balance);
        radioButton = findViewById(R.id.sim1select);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ussdService != null) {
            Toast.makeText(this, "Stopping that service", Toast.LENGTH_SHORT).show();
            stopService(ussdService);
            ussdService = null;
        }
    }

    public void fetchUpiPin(int CALLBACK_CODE) {
            Intent intent = new Intent(this, InputUpiPin.class);
            intent.putExtra("CALLBACK_CODE", String.valueOf(CALLBACK_CODE));
            startActivityForResult(intent, CODES.ENTER_UPI_PIN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("Hello!" + requestCode + ":" + resultCode);
        if (data == null)
            return;
        if (requestCode == CODES.ENTER_UPI_PIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get String data from Intent
                if (data.hasExtra("upi_pin")) {
                    upi_pin = data.getStringExtra("upi_pin").toString();
                    System.out.println("upi_pin::" + upi_pin);
                    if (data.hasExtra("CALLBACK_CODE")) {
                        int CALLBACK_CODE = Integer.valueOf(data.getStringExtra("CALLBACK_CODE").toString());

                        //check balance
                        if (CALLBACK_CODE == CODES.CALLBACK_CHECK_BALANCE_CODE) {
                            startActivityCheckBalance();
                        }
                        System.out.println("CAllBACK_CODE:" + CALLBACK_CODE);
                    }
                }
            }
        }
    }

    //   code for check balance
    public void checkBalance(View view) {
        fetchUpiPin(CODES.CALLBACK_CHECK_BALANCE_CODE);
    }

    //After fetching the upi pin this function will called
    public void startActivityCheckBalance() {
        System.out.println("Checking balance");

        if (radioButton.isChecked()) {
            simCardChoosen = 0;
        } else {
            simCardChoosen = 1;
        }
        HashMap<String, String> extraService = new HashMap<String, String>();
        extraService.put("upi_pin", upi_pin);
        extraService.put("SERVICE_CODE", String.valueOf(CODES.SERVICE_CHECK_BALANCE_CODE));

        ussdService = new Intent(this, UssdService.class);
        PhoneCall obj = new PhoneCall(this, ussdService, this);
        // this will call the phone number and start the service
        obj.phoneCall("*99*3#",extraService,simCardChoosen);
    }

    private String parseAmount(String s){
        String tmp="";
        for(int i=0;i+2<s.length();i++){
            if(s.charAt(i)=='R'&&s.charAt(i+1)=='s'&&s.charAt(i+2)=='.'){
                return s.substring(i+3);
            }
        }
        return "-1";
    }
    @Override
    public void doSomething(String status,int resultCode) {
       // EditText txtViewStatus = findViewById(R.id.txtViewStatus);
        TextView txt_amount = findViewById(R.id.txt_amount);
        txt_amount.setText("");

        if(resultCode==CODES.DIALOG_CONNECTION_ERROR_CODE){
            Toast.makeText(this, "There is a connection Error!! Try again!", Toast.LENGTH_LONG).show();
        }
        else if(resultCode==CODES.DIALOG_INVALID_PIN_ERROR_CODE){
            Toast.makeText(this, "You have Entered Wrong UPI Pin!! Try again!", Toast.LENGTH_LONG).show();
        }
        else if(resultCode == CODES.DIALOG_SUCCESS_CHECK_BALANCE_CODE){
            String amount = parseAmount(status);
            //changing amount format
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            String moneyString = formatter.format(Double.valueOf(amount));
            txt_amount.setText(moneyString);
        }
        else{
            Toast.makeText(this, "Unknown Error! ", Toast.LENGTH_LONG).show();
        }
    }
}