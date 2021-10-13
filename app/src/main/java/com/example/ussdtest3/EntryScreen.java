package com.example.ussdtest3;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;


public class EntryScreen extends AppCompatActivity {

    private TextView internetSpeed;
    private Button offlinePayButton, upiButton, creditButton, checkBalanceButton, transactionHistoryButton, walletButton;
    private FloatingActionButton fab;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS};

    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + UssdService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            System.out.println("accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            System.out.println("Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            // Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    System.out.println("-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        System.out.println("We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            System.out.println("***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }
    protected boolean checkPermissions() {
        boolean ok = true;
        //checking accesibility permission
        if (!isAccessibilitySettingsOn(getApplicationContext())) {
            Toast.makeText(this, "Please give the accessibility permission and try again!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            ok = false;
        }
        final ArrayList<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                ok = false;
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, CODES.REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(CODES.REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS, grantResults);
        }
        return ok;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODES.REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                break;
        }
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_screen);
        internetSpeed = findViewById(R.id.interntSpeed);
        upiButton = findViewById(R.id.buttonupi);
        walletButton = findViewById(R.id.buttonwallet);
        creditButton = findViewById(R.id.buttoncredit);
        checkBalanceButton = findViewById(R.id.buttoncheck);
        transactionHistoryButton = findViewById(R.id.buttonhistory);
        offlinePayButton = findViewById(R.id.buttonoffline);
        fab = findViewById(R.id.floatingActionButton);
        int usage = getIntent().getIntExtra("usage", 0);
        if (usage == 1) {
            checkBalanceButton.setVisibility(View.GONE);
            transactionHistoryButton.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
            showInternetSpeed();
        } else {
            upiButton.setVisibility(View.GONE);
            creditButton.setVisibility(View.GONE);
            walletButton.setVisibility(View.GONE);
            internetSpeed.setVisibility(View.GONE);
        }
        //checkPermissions();
    }

    public void offlinePayment(View view) {
        if(checkPermissions()) {
            Intent intent = new Intent(this, MainActivity.class);
            int usage = getIntent().getIntExtra("usage", 0);
            if(usage == 1) {
                intent.putExtra("usage", 1);
            }
            startActivity(intent);
        }
    }

    public void checkBalance(View view) {
        if(checkPermissions()) {
            Intent intent = new Intent(this, CheckBalance.class);
            startActivity(intent);
        }
    }

    public void transactionsHistory(View view) {
        if(checkPermissions()) {
            Intent intent = new Intent(this, TransactionsHistory.class);
            startActivity(intent);
        }
    }

    private void showInternetSpeed() {
        InternetSpeed obj = new InternetSpeed();
        double downSpeed = obj.getDownloadSpeed();
        String msg = "Internet conncection is stable";
        if(downSpeed < 100) {
            msg = "Poor Internet connectivity. Offline payment recommended.";
        }
        internetSpeed.setText(msg);

    }
    public void scanAndPayButtonClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("usage", 2);
        startActivity(intent);
    }
}