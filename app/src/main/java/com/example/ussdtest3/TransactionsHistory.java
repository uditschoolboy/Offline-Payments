package com.example.ussdtest3;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class TransactionsHistory extends AppCompatActivity implements ServiceCallbacks {

    /*************************************/
    //Variable for storing the upi pin, It will be fetched on the another activity
    private int simCardChoosen;

    private RadioButton radioButton;
    private RecyclerView recyclerView;

    /*************************************/

    private Intent ussdService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions_history);
        radioButton = findViewById(R.id.sim1select);
        recyclerView = findViewById(R.id.recyclerView);
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

    //   code for check history of transactions
    public void transactionsHistory(View view) {
        System.out.println("Checking transaction History");
        HashMap<String, String> extraService = new HashMap<String, String>();
        extraService.put("SERVICE_CODE", String.valueOf(CODES.SERVICE_TRANSACTIONS_HISTORY_CODE));

        ussdService = new Intent(this, UssdService.class);
        PhoneCall obj = new PhoneCall(this, ussdService, this);
        if (radioButton.isChecked()) {
            simCardChoosen = 0;
        } else {
            simCardChoosen = 1;
        }
        // this will call the phone number and start the service
        obj.phoneCall("*99*6*1#",extraService,simCardChoosen);

//        String status = "1. Sent Rs.1.00 to 7800160922@paytm on 27-Jun-21 2:42 AM \n" +
//                "2. Sent Rs.1.00 to 7800160922@paytm on 27-Jun-21 2:27 AM \n" +
//                "3. Sent Rs.1.00 to \n" +
//                "- Next7800160922@paytm on 27-Jun-21 1:50 AM \n" +
//                "4. Sent Rs.1.00 to 7800160922@paytm on 25-Jun-21 9:28 AM \n" +
//                "* More 5. Sent Rs.1.00 to 7800160922@paytm on 18-Jun-21 12:00 AM\n" +
//                "00. Back";
//        doSomething(status, 0);
    }

    @Override
    public void doSomething(String status, int resultCode) {
//        status = "1. Sent Rs.1.00 to 7800160922@paytm on 27-Jun-21 2:42 AM \n" +
//                "2. Sent Rs.1.00 to 7800160922@paytm on 27-Jun-21 2:27 AM \n" +
//                "3. Sent Rs.1.00 to \n" +
//                "- Next7800160922@paytm on 27-Jun-21 1:50 AM \n" +
//                "4. Sent Rs.1.00 to 7800160922@paytm on 25-Jun-21 9:28 AM \n";


        ArrayList<Transaction> transactions = getTransactions(status);
        if(transactions.size() == 0) {
            transactions.add(new Transaction("No transactions made", "", "", ""));
        }
        TransactionsAdapter adapter = new TransactionsAdapter(transactions);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private ArrayList<Transaction> getTransactions(String status) throws IndexOutOfBoundsException{
        int idxRs = 0, idxUPIID = 0, idxDate = 0, idxTime = 0;
        ArrayList<Transaction> transactions = new ArrayList<>();
        try {
            while (true) {
                idxRs = status.indexOf("Rs", idxTime);
                if (idxRs == -1) break;
                while (status.charAt(idxRs) > '9' || status.charAt(idxRs) < '0') {
                    idxRs++;
                }
                idxUPIID = status.indexOf("to", idxRs) + 2;
                char ch = status.charAt(idxUPIID);
                while (!((ch <= '9' && ch >= '0') || (ch >= 'A' && ch <= 'z'))) {
                    idxUPIID++;
                    ch = status.charAt(idxUPIID);
                }
                idxDate = status.indexOf("on", idxUPIID);
                while (status.charAt(idxDate) > '9' || status.charAt(idxDate) < '0') {
                    idxDate++;
                }
                idxTime = status.indexOf(" ", idxDate);
                while (status.charAt(idxTime) > '9' || status.charAt(idxTime) < '0') {
                    idxTime++;
                }
                String amount = extractData(status, 0, idxRs);
                amount = getResources().getString(R.string.Rs) + amount;
                String receiver = extractData(status, 0, idxUPIID);
                String date = extractData(status, 0, idxDate);
                date = date.replace('-', ' ');
                String time = extractData(status, 1, idxTime);
                Transaction transaction = new Transaction(amount, receiver, date, time);
                transactions.add(transaction);
            }
        } catch(IndexOutOfBoundsException e) {
            System.err.println("Index out of bounds exception");
        } finally {
            return transactions;
        }
    }
    private String extractData(String status, int spaces, int startIdx) throws IndexOutOfBoundsException{
        int lastIdx = status.indexOf(" ", startIdx);
        String result = status.substring(startIdx, lastIdx);
        if(spaces == 1) {
            if(status.charAt(lastIdx + 1) == 'A') {
                result += " AM";
            } else {
                result += " PM";
            }
        }
        return result;
    }
}