package com.example.ussdtest3;
import android.app.Activity;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.util.EventLog;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.accessibilityservice.AccessibilityServiceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class UssdService extends AccessibilityService{

    //it will store the result of the service
    //is there any connection error, invalid pin, success check balance, success transfer upi id
    private int resultCode;

    // SERVICE Codes
    private int SERVICE_CODE; //to store current service code


    // Registered callbacks, This will help to call the function of the current activity
    public static ServiceCallbacks serviceCallbacks;


    //Dialog Box code
    private int currentDialogBox;  //to store current dialog box code


    public static String TAG = "USSD";
    private String my_status;

    //it will store all text of the current dialog box
    private String dialog_txt;


    //In transcation hisory, there can be multiple dialog box
    private static ArrayList<String> transaction_history;

    private String upi_pin,upi_id,remarks,amount;

    private AccessibilityNodeInfo sendButton, okButton, cancelButton, editText;

    public static Activity activity;


    //This function will detect the type of dialog box and the buttons address
    public void dfs(AccessibilityNodeInfo currentNode){
        if(currentNode==null)
            return;

        int childCount = currentNode.getChildCount();
        System.out.println("Total children: " + childCount);
        System.out.println("Current node: "+ currentNode);

        for(int i = 0;i<childCount;i++) {
            AccessibilityNodeInfo child = currentNode.getChild(i);
            if(child==null)
                continue;
            CharSequence txt = child.getText();

            System.out.println("Child :" + i);
            System.out.println("Class name: " + child.getClassName());
            int subChildCount = child.getChildCount();
            System.out.println("Total subchildren children: " + subChildCount);

            if(txt!=null && child.getClassName().equals(Button.class.getName())){
                System.out.println("Hi Text: " + txt.toString());
                Toast.makeText(this, "Text: " + txt.toString(), Toast.LENGTH_SHORT).show();
                if (txt.toString().toLowerCase().trim().equals("send")) {
                    sendButton = child;
                }

                if (txt.toString().toLowerCase().trim().equals("ok")) {
                    okButton = child;
                }

                if (txt.toString().toLowerCase().trim().equals("cancel")) {
                    cancelButton = child;
                }
            }
            else if(txt!=null) {


                if(dialog_txt.length()==0)
                    dialog_txt=txt.toString();
                else{
                    dialog_txt+=" ";
                    dialog_txt+=txt.toString();
                }

                my_status+=txt.toString();
                Toast.makeText(this, "content:: "+txt.toString(), Toast.LENGTH_LONG).show();

                //checking if this dialog box is for entering UPI Pin or not
                {
                    ArrayList<String> arrayList =new ArrayList<String>();
                    arrayList.add("Enter");
                    arrayList.add("UPI");
                    arrayList.add("PIN");
                    System.out.println("ArrayList for detecting the type of dialog box: "+arrayList);
                    System.out.println("My UPI pin: "+upi_pin);
                    if(isStringContainsArrayList(txt.toString(),arrayList)){
                        currentDialogBox = CODES.DIALOG_ENTER_PIN_CODE; //Enter UPI PIN
                        Toast.makeText(this, "This is UPI pin box", Toast.LENGTH_LONG).show();
                    }
                }


                //checking if this dialog box is for entering Amount
                {
                    ArrayList<String> arrayList =new ArrayList<String>();
                    arrayList.add("Enter");
                    arrayList.add("Amount");
                    arrayList.add("in");
                    arrayList.add("Rs");
                    System.out.println("ArrayList for detecting the type of dialog box: "+arrayList);
                    System.out.println("My UPI pin: "+upi_pin);
                    if(isStringContainsArrayList(txt.toString(),arrayList)){
                        currentDialogBox = CODES.DIALOG_ENTER_AMOUNT_CODE; //Enter amount
                        Toast.makeText(this, "This is box for entering amount", Toast.LENGTH_LONG).show();
                    }
                }

                //checking if this dialog box is for entering remark
                {
                    ArrayList<String> arrayList =new ArrayList<String>();
                    arrayList.add("Enter");
                    arrayList.add("a");
                    arrayList.add("Remark");
                    arrayList.add("to");
                    arrayList.add("skip");
                    System.out.println("ArrayList for detecting the type of dialog box: "+arrayList);
                    System.out.println("My UPI pin: "+upi_pin);
                    if(isStringContainsArrayList(txt.toString(),arrayList)){
                        currentDialogBox = CODES.DIALOG_ENTER_REMARKS_CODE; //Enter Remark
                        Toast.makeText(this, "This is Remarks box", Toast.LENGTH_LONG).show();
                    }
                    else{
//                        Toast.makeText(this, "Failed to detect Remark",Toast.LENGTH_LONG ).show();
                    }
                }

                //checking if this dialog box is for entering UPI Id or not
                {
                    ArrayList<String> arrayList =new ArrayList<String>();
                    arrayList.add("Enter");
                    arrayList.add("UPI");
                    arrayList.add("ID");
                    System.out.println("ArrayList for detecting the type of dialog box: "+arrayList);
                    System.out.println("My UPI pin: "+upi_pin);
                    if(isStringContainsArrayList(txt.toString(),arrayList)){
                        currentDialogBox = CODES.DIALOG_ENTER_UPI_ID_CODE; //Enter UPI ID
                        Toast.makeText(this, "This is UPI ID box", Toast.LENGTH_LONG).show();
                    }
                    else{
//                        Toast.makeText(this, "Failed to detect UPI id",Toast.LENGTH_LONG ).show();
                    }
                }


                //checking if this dialog box is sucsess box for the check balance
                {
                    ArrayList<String> arrayList =new ArrayList<String>();
                    arrayList.add("Your");
                    arrayList.add("account");
                    arrayList.add("balance");
                    arrayList.add("is");
                    arrayList.add("Rs");
                    System.out.println("ArrayList for detecting the type of dialog box: "+arrayList);
                    System.out.println("My UPI pin: "+upi_pin);
                    if(isStringContainsArrayList(txt.toString(),arrayList)){
                        currentDialogBox = CODES.DIALOG_SUCCESS_CHECK_BALANCE_CODE; //Enter UPI ID
                        Toast.makeText(this, "This is check balance success box", Toast.LENGTH_LONG).show();
                    }
                    else{
//                        Toast.makeText(this, "Failed to detect check balance success box",Toast.LENGTH_LONG ).show();
                    }
                }

                //checking if this dialog box is success box for the payment successful in upi transfer
                {
                    ArrayList<String> arrayList =new ArrayList<String>();
                    arrayList.add("Your");
                    arrayList.add("payment");
                    arrayList.add("to");
                    arrayList.add("is");
                    arrayList.add("successful");
                    System.out.println("ArrayList for detecting the type of dialog box: "+arrayList);
                    System.out.println("My UPI pin: "+upi_pin);
                    if(isStringContainsArrayList(txt.toString(),arrayList)){
                        currentDialogBox = CODES.DIALOG_SUCCESS_TRANSFER_UPI_CODE; //Enter UPI ID
                        Toast.makeText(this, "This is upi payment success box", Toast.LENGTH_LONG).show();
                    }
                    else{
//                        Toast.makeText(this, "Failed to detect upi payment success box",Toast.LENGTH_LONG ).show();
                    }
                }

                //checking if this dialog box is showing connection error
                {
                    ArrayList<String> arrayList =new ArrayList<String>();
                    arrayList.add("Connection");
                    arrayList.add("Problem");
                    arrayList.add("or");
                    arrayList.add("Invalid");
                    arrayList.add("MMI");
                    arrayList.add("code");
                    System.out.println("ArrayList for connection error dialog box: "+arrayList);
                    if(isStringContainsArrayList(txt.toString(),arrayList)){
                        currentDialogBox = CODES.DIALOG_CONNECTION_ERROR_CODE; //Enter UPI PIN
                    }
                }

            }
            else if (child.getClassName().equals(ScrollView.class.getName())) {
                dfs(child);
            }
            else if(child.getClassName().equals(EditText.class.getName())){
                editText = child;
            }
        }
    }

    //public static Activity activity;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        upi_pin="";
        amount="";
        remarks="";
        upi_id="";
        if(transaction_history==null){
            transaction_history=new ArrayList<String>();
        }

        if(intent!=null) {
            if (intent.hasExtra("upi_pin")) {
                upi_pin = intent.getStringExtra("upi_pin");
            }
            if (intent.hasExtra("upi_id")) {
                upi_id = intent.getStringExtra("upi_id");
            }
            if (intent.hasExtra("remarks")) {
                remarks = intent.getStringExtra("remarks");
            }
            if (intent.hasExtra("amount")) {
                amount = intent.getStringExtra("amount");
            }

            if (intent.hasExtra("SERVICE_CODE")) {
                SERVICE_CODE = Integer.valueOf(intent.getStringExtra("SERVICE_CODE").toString());
                System.out.println("SERVICE CODE: "+SERVICE_CODE);
            }
        }
        System.out.println("Inside service: "+ upi_pin);
        return START_STICKY;
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        currentDialogBox=-1;
        sendButton=null;
        okButton=null;
        cancelButton=null;
        editText=null;
        my_status="";
        dialog_txt="";
        resultCode=-1;


        System.out.println("onAccessibilityEvent");
        String text = event.getText().toString();
        if (event.getClassName().equals("android.app.AlertDialog")) {
            System.out.println(text);
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }



        if (event.getClassName().equals("android.app.AlertDialog")) {
            AccessibilityNodeInfo currentNode = event.getSource(); //getRootInActiveWindow();
            //   event.hide();
            try {
                dfs(currentNode);
            } catch(Exception e) {
                Toast.makeText(this, e+"Error happened", Toast.LENGTH_LONG).show();
            }

//            Toast.makeText(this, "codes: "+SERVICE_CODE+":"+currentDialogBox+" -"+my_status, Toast.LENGTH_LONG).show();




            System.out.println("edtText"+editText);
            System.out.println("okButton"+okButton);
            System.out.println("cancelButton"+cancelButton);
            System.out.println("sendButton"+sendButton);


            //checking if the dialog box is invalid upi pin
            {
                ArrayList<String> arrayList =new ArrayList<String>();
                arrayList.add("Invalid");
                arrayList.add("UPI");
                arrayList.add("PIN,");

                if(isSubtring(dialog_txt,arrayList)) {
                    okButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Toast.makeText(this, "ok button is clicked", Toast.LENGTH_SHORT).show();
                    resultCode=CODES.DIALOG_INVALID_PIN_ERROR_CODE;
                    sendDataToActivity();
                    return;
                }
            }

            if(SERVICE_CODE==CODES.SERVICE_CHECK_BALANCE_CODE){

                //entering the UPI Pin Dialog box
                if(currentDialogBox==CODES.DIALOG_ENTER_PIN_CODE){
                    if(editText!=null && sendButton!=null){
                        //Entering upi pin
                        try {
                            fillDataInTextField(editText, upi_pin);
                        } catch (Exception e) {
                            Toast.makeText(this, e + "Error happened in the input box", Toast.LENGTH_LONG).show();
                        }
                        sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Toast.makeText(this, "send button is clicked", Toast.LENGTH_SHORT).show();
                    }
                }

                //suceess page for check balance
                if(currentDialogBox==CODES.DIALOG_SUCCESS_CHECK_BALANCE_CODE){
                    if(okButton!=null) {
                        okButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Toast.makeText(this, "ok button is clicked", Toast.LENGTH_SHORT).show();
                        resultCode=CODES.DIALOG_SUCCESS_CHECK_BALANCE_CODE;
                        sendDataToActivity();
                    }
                }
            }

            if(SERVICE_CODE==CODES.SERVICE_TRANSACTIONS_HISTORY_CODE){
                System.out.println("Current service code: "+ SERVICE_CODE);
                ArrayList<String> arrayList1 =new ArrayList<String>();
                arrayList1.add("Sent");
                arrayList1.add("Rs.");
                arrayList1.add("to");
                arrayList1.add("on");

                ArrayList<String> arrayList2 =new ArrayList<String>();
                arrayList2.add("Failed");
                arrayList2.add("Rs.");
                arrayList2.add("to");
                arrayList2.add("send");


                if(isSubtring(dialog_txt,arrayList1)||isSubtring(dialog_txt, arrayList2)|| dialog_txt.contains("- Next")||dialog_txt.contains("* More")){

                    currentDialogBox = CODES.DIALOG_TRANSACTIONS_HISTORY;
                    Toast.makeText(this, "This is Transaction history Dialog Box", Toast.LENGTH_LONG).show();

                    transaction_history.add(dialog_txt);

                    if(editText!=null && sendButton!=null){
                        if(dialog_txt.contains("- Next")) {
                            try {
                                fillDataInTextField(editText, "-");
                            } catch (Exception e) {
                                Toast.makeText(this, e + "Error happened in the upi id input box", Toast.LENGTH_LONG).show();
                            }
                            sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            return;
                        }
                        else if(dialog_txt.contains("* More")){
                            try {
                                fillDataInTextField(editText, "*");
                            } catch (Exception e) {
                                Toast.makeText(this, e + "Error happened in the upi id input box", Toast.LENGTH_LONG).show();
                            }
                            sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            return;
                        }

                    }
                    if(cancelButton!=null && dialog_txt.contains("00. Back")){
                        cancelButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        sendDataToActivity();
                        return;
                    }
                }
            }


            if(SERVICE_CODE==CODES.SERVICE_TRANSFER_UPI_CODE){
                System.out.println("SERVICE_TRANSFER_UPI_CODE");
                System.out.println(upi_id);
                System.out.println(upi_pin);
                System.out.println(amount);
                System.out.println(remarks);
                if(currentDialogBox==CODES.DIALOG_ENTER_UPI_ID_CODE){
                    if(sendButton!=null&&editText!=null){
                        //Entering upi id
                        try {
                            fillDataInTextField(editText, upi_id);
                        } catch (Exception e) {
                            Toast.makeText(this, e + "Error happened in the upi id input box", Toast.LENGTH_LONG).show();
                        }
                        sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Toast.makeText(this, "send button is clicked", Toast.LENGTH_SHORT).show();
                    }
                }

                if(currentDialogBox==CODES.DIALOG_ENTER_AMOUNT_CODE){
                    if(sendButton!=null&&editText!=null){
                        //Entering amount
                        try {
                            fillDataInTextField(editText, amount);
                        } catch (Exception e) {
                            Toast.makeText(this, e + "Error happened in the amount input box", Toast.LENGTH_LONG).show();
                        }
                        sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Toast.makeText(this, "send button is clicked", Toast.LENGTH_SHORT).show();
                    }
                }


                if(currentDialogBox==CODES.DIALOG_ENTER_REMARKS_CODE){
                    if(sendButton!=null&&editText!=null){
                        //Entering remarks
                        try {
                            fillDataInTextField(editText, remarks);
                        } catch (Exception e) {
                            Toast.makeText(this, e + "Error happened in the remark input box", Toast.LENGTH_LONG).show();
                        }
                        sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Toast.makeText(this, "send button is clicked", Toast.LENGTH_SHORT).show();
                    }
                }

                //entering the UPI Pin Dialog box
                if(currentDialogBox==CODES.DIALOG_ENTER_PIN_CODE){
                    if(editText!=null && sendButton!=null){
                        //Entering upi pin
                        try {
                            fillDataInTextField(editText, upi_pin);
                        } catch (Exception e) {
                            Toast.makeText(this, e + "Error happened in the input box", Toast.LENGTH_LONG).show();
                        }

                        ArrayList<String> a = new ArrayList<String>();
                        a.add("UPI ID-"+upi_id);
                        a.add("Amount "+amount);

                        if(isSubtring(dialog_txt,a)) {
                            dialog_txt+="$SUCCESSFUL PAYMENT$";
                            sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Toast.makeText(this, "send button is clicked", Toast.LENGTH_SHORT).show();

                        }
                        else{
                            dialog_txt+="$ Invalid Transaction details $";
                            cancelButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            return;
                        }
                    }
                }

                //suceess page for upi transfer
                if(currentDialogBox==CODES.DIALOG_SUCCESS_TRANSFER_UPI_CODE){
                    if(cancelButton!=null){
                        cancelButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Toast.makeText(this, "cancel button is clicked", Toast.LENGTH_SHORT).show();
                        resultCode=CODES.DIALOG_SUCCESS_TRANSFER_UPI_CODE;
                        sendDataToActivity();
                    }
                }
            }

            //Handling Connection error dialog box
            if(currentDialogBox==CODES.DIALOG_CONNECTION_ERROR_CODE){

                System.out.println("Error in connection or invalid MMI code!!!");
                okButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                Toast.makeText(this, "ok button is clicked", Toast.LENGTH_SHORT).show();
//                Toast.makeText(this, "There is some issues in the connection or invalid MMI", Toast.LENGTH_LONG).show();

                resultCode=CODES.DIALOG_CONNECTION_ERROR_CODE;
                sendDataToActivity();
            }
//            if(okButton!=null){
//                Toast.makeText(this, "ok button is clicked", Toast.LENGTH_SHORT).show();
//                okButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//            }
        }
    }

    @Override
    public void onInterrupt() {

    }


    //return true if S contains all string of 'a' as a subtring in S
    private boolean isSubtring(String S,ArrayList<String> a){
        for(String x:a)
        {
            if(!S.contains(x))
                return false;
        }
        return true;
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    private void sendDataToActivity(){

        if(serviceCallbacks!=null){
            if(transaction_history!=null&&transaction_history.size()>0&&SERVICE_CODE==CODES.SERVICE_TRANSACTIONS_HISTORY_CODE) {
                dialog_txt="";
                for (int i = 0; i < transaction_history.size(); i++)
                    dialog_txt += transaction_history.get(i);
                transaction_history.clear();
            }
            serviceCallbacks.doSomething(dialog_txt,resultCode);
        }

        //  TextView txtViewStatus = activity.findViewById(R.id.txtViewStatus);
        //  txtViewStatus.setText(my_status+", Serivice code:"+SERVICE_CODE+" Dialog txt:"+dialog_txt);
    }
    private void fillDataInTextField(AccessibilityNodeInfo edit, String value) {
        if ((value == null) || (edit == null))
            return;
        Bundle b = new Bundle();
        b.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value);
        edit.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, b);
    }

    private boolean isStringContainsArrayList(String toString, ArrayList<String> arrayList) {
        //toString="KUMAR,\nEnter Amount in Rs.";
        for(int i=0;i<arrayList.size();i++){
            arrayList.set(i,arrayList.get(i).trim().toLowerCase());
        }
        String tmp="";
        HashSet<String> st = new HashSet<String>();
        for(int i=0;i<toString.length();i++){
            if(toString.charAt(i)==' '||toString.charAt(i)=='.'||toString.charAt(i)==','||toString.charAt(i)=='\n'||toString.charAt(i)=='('||toString.charAt(i)==')')
                continue;
            tmp+=toString.charAt(i);
            if(i+1<toString.length()&&toString.charAt(i+1)!=' '&&toString.charAt(i+1)!='.'&&toString.charAt(i+1)!=','&&toString.charAt(i+1)!='\n'&&toString.charAt(i+1)!='('&&toString.charAt(i+1)!=')')
                continue;
            st.add(tmp.toLowerCase());
            tmp="";
        }
        for(int i=0;i<arrayList.size();i++){
            if(!st.contains(arrayList.get(i))){
                System.out.println(arrayList.get(i)+": not found");
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        System.out.println("onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.packageNames = new String[]{"com.android.phone"};
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
    }
}