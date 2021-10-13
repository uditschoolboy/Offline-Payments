package com.example.ussdtest3;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.PhoneNumberUtils;

import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ServiceCallbacks {

    private String textUpiId, textAmount ,textRemarks;

    /*************************************/
    //Variables for storing the upi pin, It will be fetched on the another activity
    private EditText editTextUpiPin, editTextUpiId, editTextAmount, editTextRemarks;
    private RadioButton radioButton;
    public static TextView txtViewStatus;
    private String upi_pin;
    private int simCardChoosen;
    /*************************************/


    private Intent ussdService;

    /*************************************/
    //Variables for sending the money
    private EditText remarks, amount, upi_id;
    private ImageButton scanAndPayButton;
    private Button sendMoneyButton;
    /*************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        radioButton = findViewById(R.id.sim1select);
       // MyReceiver.activity = this;
        editTextUpiId = findViewById(R.id.editTextUpiId);
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextRemarks = findViewById(R.id.editTextRemarks);
        scanAndPayButton = findViewById(R.id.buttonScanAndPay);
        sendMoneyButton = findViewById(R.id.buttonSendMoney);

        Intent intent = getIntent();
        int usage = intent.getIntExtra("usage", 0);
        if(usage == 1) {
            scanAndPayButton.setVisibility(View.GONE);
            editTextAmount.setEnabled(false);
            editTextUpiId.setEnabled(false);
            editTextRemarks.setEnabled(false);

            editTextAmount.setText("1");
            editTextUpiId.setText("7800160922@paytm");
            editTextRemarks.setText("demo");

            sendMoney(sendMoneyButton);
        } else if(usage == 2) {
            scanAndPay(scanAndPayButton);
        }
        String action = intent.getAction();
        Uri data = intent.getData();
        if(data!=null) {
            System.out.println("Upid id through link: " + data);
            String upi_id=data.getQueryParameter("upi_id");
            String amount=data.getQueryParameter("amount");
            String remarks=data.getQueryParameter("remarks");
            if(upi_id!=null&&amount!=null&&remarks!=null) {
                System.out.println(upi_id);
                System.out.println(amount);
                System.out.println(remarks);
                editTextUpiId.setText(upi_id);
                editTextAmount.setText(amount);
                editTextRemarks.setText(remarks);
            }
        }
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

    //    public void dialButtonClicked(View view) {
//
//        EditText editText = findViewById(R.id.editTextPhoneNumber);
//        String number = editText.getText().toString();
//        if (checkPermissions()) {
//            HashMap<String, String> extraService = new HashMap<String, String>();
//            phoneCall(number, extraService);
//        }
//    }

    private void phoneCall(String number, HashMap<String, String> extraService) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        Uri uri = null;
        try {
            uri = numberToUri(number);
        } catch (Exception e) {
            Toast.makeText(this, "Error in function", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            String ussdNumber = Uri.decode(uri.toString());
            Toast.makeText(this, ussdNumber, Toast.LENGTH_LONG).show();
            callIntent.setData(uri);
            callIntent.putExtra("com.android.phone.force.slot", true);
            callIntent.putExtra("Cdma_Supp", true);
            final String simSlotName[] = {"extra_asus_dial_use_dualsim", "com.android.phone.extra.slot", "slot", "simslot", "sim_slot", "subscription", "Subscription", "phone", "com.android.phone.DialingMode", "simSlot", "slot_id", "simId", "simnum", "phone_type", "slotId", "slotIdx"};

            //simCardChoosen
            if (radioButton.isChecked()) {
                simCardChoosen = 0;
            } else {
                simCardChoosen = 1;
            }
            Toast.makeText(this, "calling with sim card " + (simCardChoosen + 1), Toast.LENGTH_SHORT).show();
            for (String s : simSlotName) {
                callIntent.putExtra(s, simCardChoosen); // simNumber = 0 or 1 according to sim.......
            }
            //0 for sim1

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                TelecomManager telecomManager = (TelecomManager) this.getSystemService(Context.TELECOM_SERVICE);
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    List<PhoneAccountHandle> phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
                    if (phoneAccountHandleList != null && phoneAccountHandleList.size() > simCardChoosen)
                        callIntent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(simCardChoosen));
                } catch (Exception e) {
                }
            }
            ussdService = new Intent(this, UssdService.class);
            for (String name : extraService.keySet()) {
                ussdService.putExtra(name, extraService.get(name));
            }
            UssdService.activity = this;
            startService(ussdService);
            startActivity(callIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Some error in intent part", Toast.LENGTH_LONG).show();
        }
    }

    private Uri numberToUri(String number) throws Exception {
        if (number.endsWith("#")) {
            number = number.replace('#', ' ');
            number = number.trim();
        }
        String ussdStr = number + Uri.encode("#");
        Uri uri = Uri.parse("tel:" + ussdStr);
        System.out.println(uri);
        return uri;
    }

    public void fetchUpiPin(int CALLBACK_CODE) {
      //  if (checkPermissions()) {
            Intent intent = new Intent(this, InputUpiPin.class);
            intent.putExtra("CALLBACK_CODE", String.valueOf(CALLBACK_CODE));
            startActivityForResult(intent, CODES.ENTER_UPI_PIN_REQUEST_CODE);
        //}
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

                        //send money
                        else if (CALLBACK_CODE == CODES.CALLBACK_SEND_MONEY_CODE) {
                            startActivitySendMoney();
                        }
                        System.out.println("CAllBACK_CODE:" + CALLBACK_CODE);
                    }
                }
            }
        }
        else if(requestCode==CODES.SCAN_REQUEST_CODE){
            if (resultCode == RESULT_OK) {
                // Get String data from Intent
                if (data.hasExtra("upi_id")) {
                    String upi_id = data.getStringExtra("upi_id").toString();
                    System.out.println("upi_id::" + upi_id);

                    //After scanning upi id
                    if(editTextUpiId!=null)
                    editTextUpiId.setText(upi_id);
                }
            }
        }
    }


    //   code for check balance
    public void checkBalance(View view) {
        fetchUpiPin(CODES.CALLBACK_CHECK_BALANCE_CODE);
    }

    public void startActivityCheckBalance() {
        System.out.println("Checking balance");
        HashMap<String, String> extraService = new HashMap<String, String>();
        extraService.put("upi_pin", upi_pin);
        extraService.put("SERVICE_CODE", String.valueOf(CODES.SERVICE_CHECK_BALANCE_CODE));
        phoneCall("*99*3#", extraService);
    }

    // code for sending money
    public void sendMoney(View view) {
        fetchUpiPin(CODES.CALLBACK_SEND_MONEY_CODE);
    }

    public void startActivitySendMoney() {

        textUpiId = editTextUpiId.getText().toString();
        textAmount = editTextAmount.getText().toString();
        textRemarks = editTextRemarks.getText().toString();

        System.out.println("Sending Money");
        HashMap<String, String> extraService = new HashMap<String, String>();
        extraService.put("upi_pin", upi_pin);
        extraService.put("upi_id", textUpiId); //9504622372@ybl
        extraService.put("amount", textAmount);
        extraService.put("remarks", textRemarks);
        extraService.put("SERVICE_CODE", String.valueOf(CODES.SERVICE_TRANSFER_UPI_CODE));
        System.out.println("Extra service : " + extraService);
        //phoneCall("*99*1*3#", extraService);

        ussdService = new Intent(this, UssdService.class);
        PhoneCall obj = new PhoneCall(this, ussdService ,this);
       // UssdService.serviceCallbacks=this;
        if (radioButton.isChecked()) {
            simCardChoosen = 0;
        } else {
            simCardChoosen = 1;
        }
        obj.phoneCall("*99*1*3#",extraService,simCardChoosen);
    }

//    public void sendSms(View view) {
//        if (!checkPermissions())
//            return;
//        int simCardChoosen;
//        RadioButton SMSradioButton = findViewById(R.id.SMSsim1select);
//        if (SMSradioButton.isChecked()) {
//            simCardChoosen = 0;
//        } else {
//            simCardChoosen = 1;
//        }
//
//        EditText editTextSMSPhone = (EditText) findViewById(R.id.editTextSMSPhone);
//        // Set the destination phone number to the string in editText.
//        String destinationAddress = editTextSMSPhone.getText().toString();
//        ;
//        // Find the sms_message view.
//        EditText editTextSMS = findViewById(R.id.editTextSMS);
//        // Get the text of the SMS message.
//        String smsMessage = editTextSMS.getText().toString();
//        if (smsMessage.length() == 0 || destinationAddress.length() == 0) {
//            return;
//        }
//        // Set the service center address if needed, otherwise null.
//        String scAddress = null;
//        // Set pending intents to broadcast
//        // when message sent and when delivered, or set to null.
//        PendingIntent sentIntent = null, deliveryIntent = null;
//
//
//        // Use SmsManager.
//        SmsManager smsManager = SmsManager.getDefault();
//
//        String dbg="";
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//            SubscriptionManager localSubscriptionManager = SubscriptionManager.from(this);
//
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//            System.out.println("localSubscriptionManager.getActiveSubscriptionInfoCount():" + localSubscriptionManager.getActiveSubscriptionInfoCount());
//            if (localSubscriptionManager.getActiveSubscriptionInfoCount() <= simCardChoosen) {
//                Toast.makeText(this, "Unable to find the sim" + simCardChoosen, Toast.LENGTH_SHORT).show();
//                simCardChoosen = 0;
//            }
//            List localList = localSubscriptionManager.getActiveSubscriptionInfoList();
//
////                for(int i=0;i<localList.size();i++){
////                    dbg+="  child 1: "+((SubscriptionInfo) localList.get(simCardChoosen)).getSubscriptionId();
////                }
////                System.out.println("Debug:"+dbg);
////                TextView txtViewDebugStatus = findViewById(R.id.txtViewDebugStatus);
////                txtViewDebugStatus.setText(dbg);
//
//            if (smsMessage.length() > 160) {
//                final ArrayList<String> messageParts = smsManager.divideMessage(smsMessage);
//                SmsManager.getSmsManagerForSubscriptionId(((SubscriptionInfo) localList.get(simCardChoosen)).getSubscriptionId()).sendMultipartTextMessage(destinationAddress, scAddress, messageParts, null, null);
//                Toast.makeText(this, "Message has been sent: Sim" + simCardChoosen + " ,message: " + smsMessage, Toast.LENGTH_LONG).show();
//                return;
//            } else {
//                SmsManager.getSmsManagerForSubscriptionId(((SubscriptionInfo) localList.get(simCardChoosen)).getSubscriptionId()).sendTextMessage(destinationAddress, scAddress, smsMessage, null, null);
//            }
//            System.out.println(destinationAddress + ":" + smsMessage);
//        }
//    }

    public void scanAndPay(View view) {
        //TODO scan and pay service or intent
        Intent intent = new Intent(this, ScanQrCode.class);
        startActivityForResult(intent,CODES.SCAN_REQUEST_CODE);
    }


    private String parseUserName(String S){
        String s="";
        for(int i=0;i<S.length();i++){
            if(S.charAt(i)=='\n')
                s+=" ";
            else
                s+=S.charAt(i);
        }

        String tmp="";
        String r = "Your payment to";
        int index = s.indexOf(r);
        if(index!=-1){
            index+=r.length();
            while(index<s.length()){
                if(s.charAt(index)==',')
                    break;
                tmp+=s.charAt(index);
                index+=1;
            }
        }
        return tmp;
    }
    private String parseReferenceId(String S){
        String s="";
        for(int i=0;i<S.length();i++){
            if(S.charAt(i)=='\n')
                s+=" ";
            else
                s+=S.charAt(i);
        }

        String tmp="";
        String r = "RefId:";
        int index = s.indexOf(r);
        if(index!=-1){
            index+=r.length();
            while(index<s.length()){
                if(s.charAt(index)==')')
                    break;
                tmp+=s.charAt(index);
                index+=1;
            }
        }

        return tmp;
    }
    @Override
    public void doSomething(String status, int resultCode) {



//        EditText txtViewStatus = findViewById(R.id.txtViewStatus);
//        txtViewStatus.setText(status);


        if(resultCode==CODES.DIALOG_CONNECTION_ERROR_CODE){
            Toast.makeText(this, "There is a connection Error!! Try again!", Toast.LENGTH_LONG).show();
        }
        else if(resultCode==CODES.DIALOG_INVALID_PIN_ERROR_CODE){
            Toast.makeText(this, "You have Entered Wrong UPI Pin!! Try again!", Toast.LENGTH_LONG).show();
        }
        else if(resultCode == CODES.DIALOG_SUCCESS_TRANSFER_UPI_CODE){
//            txtViewStatus.setText(status+":1");
            Intent intent = new Intent(this, PaymentUPISuccessful.class);
            intent.putExtra("upi_id", textUpiId);
            intent.putExtra("amount", textAmount);
            intent.putExtra("reference_id", parseReferenceId(status));
            intent.putExtra("user_name", parseUserName(status));
            startActivity(intent);

            return;
        }
        else{
            Toast.makeText(this, "Unknown Error! ", Toast.LENGTH_LONG).show();
        }
    }
}