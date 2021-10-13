package com.example.ussdtest3;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import java.util.HashMap;
import java.util.List;

public class PhoneCall {
    private Activity mthis;
    private Intent ussdService;
    private ServiceCallbacks serviceCallbacks;
    //mthis1 is the class of current object
    //ussdService is the object of service class
    PhoneCall(Activity mthis, Intent ussdService, ServiceCallbacks serviceCallbacks){
        this.mthis=mthis;
        this.ussdService=ussdService;
        this.serviceCallbacks=serviceCallbacks;
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

    public void phoneCall(String number, HashMap<String, String> extraService, int simCardChoosen) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        Uri uri = null;
        try {
            uri = numberToUri(number);
        } catch (Exception e) {
            Toast.makeText(mthis, "Error in function", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            String ussdNumber = Uri.decode(uri.toString());
            Toast.makeText(mthis, ussdNumber, Toast.LENGTH_LONG).show();
            callIntent.setData(uri);
            callIntent.putExtra("com.android.phone.force.slot", true);
            callIntent.putExtra("Cdma_Supp", true);
            final String simSlotName[] = {"extra_asus_dial_use_dualsim", "com.android.phone.extra.slot", "slot", "simslot", "sim_slot", "subscription", "Subscription", "phone", "com.android.phone.DialingMode", "simSlot", "slot_id", "simId", "simnum", "phone_type", "slotId", "slotIdx"};


//            Toast.makeText(mthis, "calling with sim card " + (simCardChoosen + 1), Toast.LENGTH_SHORT).show();
            for (String s : simSlotName) {
                callIntent.putExtra(s, simCardChoosen); // simNumber = 0 or 1 according to sim.......
            }
            //0 for sim1

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                TelecomManager telecomManager = (TelecomManager) mthis.getSystemService(Context.TELECOM_SERVICE);
                try {
                    if (ActivityCompat.checkSelfPermission(mthis, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    List<PhoneAccountHandle> phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
                    if (phoneAccountHandleList != null && phoneAccountHandleList.size() > simCardChoosen)
                        callIntent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(simCardChoosen));
                } catch (Exception e) {
                }
            }
            //ussdService = new Intent(mthis, UssdService.class);
            for (String name : extraService.keySet()) {
                ussdService.putExtra(name, extraService.get(name));
            }
            UssdService.activity = mthis;
            UssdService.serviceCallbacks=this.serviceCallbacks;
            mthis.startService(ussdService); //starting the service
            mthis.startActivity(callIntent); //calling the number
        } catch (Exception e) {
            Toast.makeText(mthis, "Some error in intent part", Toast.LENGTH_LONG).show();
        }
    }
}
