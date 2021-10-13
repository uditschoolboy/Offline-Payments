package com.example.ussdtest3;

//callback code will be used to call the respective function after fetching upipin
//service code will be used to open give id to the current process, It will be used to parse the dialog box inside ussd service
public class CODES {
    public final static int DIALOG_ENTER_PIN_CODE=1;
    public final static int DIALOG_CONNECTION_ERROR_CODE=2;
    public final static int DIALOG_ENTER_UPI_ID_CODE=3;
    public final static int DIALOG_ENTER_AMOUNT_CODE=4;
    public final static int DIALOG_ENTER_REMARKS_CODE=5;
    public final static int DIALOG_SUCCESS_CHECK_BALANCE_CODE=6;
    public final static int DIALOG_SUCCESS_TRANSFER_UPI_CODE=7;

    public final static int REQUEST_CODE_ASK_PERMISSIONS = 8;
    public final static int CALLBACK_SEND_MONEY_CODE = 9;
    public final static int ENTER_UPI_PIN_REQUEST_CODE = 10;
    public final static int CALLBACK_CHECK_BALANCE_CODE = 11;
    public final static int SERVICE_TRANSFER_UPI_CODE = 13;
    public final static int SERVICE_CHECK_BALANCE_CODE = 14;
    public final static int CALLBACK_CHECK_TRANSACTIONS_HISTORY_CODE=15;
    public final static int SERVICE_TRANSACTIONS_HISTORY_CODE=16;
    public final static int CALLBACK_SCAN_UPI_ID_CODE=17;
    public final static int SCAN_REQUEST_CODE=18;

    public final static int DIALOG_TRANSACTIONS_HISTORY=19;
    public final static int DIALOG_INVALID_PIN_ERROR_CODE=20;
}
