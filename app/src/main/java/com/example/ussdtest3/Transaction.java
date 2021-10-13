package com.example.ussdtest3;

class Transaction {
    private String amount, receiver, date, time;
    Transaction() {
        //for use by the handler class
    }
    Transaction(String amount, String receiver, String date, String time) {
        this.amount = amount;
        this.receiver = receiver;
        this.date = date;
        this.time = time;
    }

    public String getAmount() {
        return amount;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
