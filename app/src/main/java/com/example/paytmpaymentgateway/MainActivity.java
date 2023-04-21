package com.example.paytmpaymentgateway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.github.kittinunf.fuel.core.ResponseHandler;
import com.google.gson.JsonIOException;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.paytm.pgsdk.TransactionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    EditText amountEditText;
    Button payBtn;
    String orderID;
    String callbackurl;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        amountEditText = findViewById(R.id.amountEditText);
        payBtn = findViewById(R.id.pay);

        orderID = String.valueOf(UUID.randomUUID());
        callbackurl = "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID=" + orderID;

        payBtn.setOnClickListener(view ->{
            getTransactionToken(orderID, amountEditText.getText().toString());
        });

        
    }

    private void getTransactionToken(String orderID, String amount){
        Fuel.INSTANCE.post("https://us-central1-paytm-payments-46d03.cloudfunctions.net/InitiateTransactionApi" + "?amt=" + amount
                + "&oid=" + orderID, null).responseString(new ResponseHandler<String>() {
            @Override
            public void success(@NonNull Request request, @NonNull Response response, String s) {
                try {
                    JSONObject result = new JSONObject(s);
                    JSONObject paytmResponse = new JSONObject(result.getString("Response"));
                    String merchantID = result.getString("MerchantID");
                    String txnToken = paytmResponse.getJSONObject("body").getString("txnToken");

                    invokePaytmSdk(merchantID, txnToken);

                } catch (JSONException e){
                    Log.e("Json error", e.getMessage());
                }
            }

            @Override
            public void failure(@NonNull Request request, @NonNull Response response, @NonNull FuelError fuelError) {
                Log.e("Error retrieving", fuelError.toString());
            }
        });
    }

    private void invokePaytmSdk(String merchantID, String txnToken){
        PaytmOrder paytmOrder = new PaytmOrder(orderID, merchantID, txnToken, amountEditText.getText().toString(), callbackurl);
        TransactionManager transactionManager = new TransactionManager(paytmOrder, new PaytmPaymentTransactionCallback() {
            @Override
            public void onTransactionResponse(@Nullable Bundle bundle) {

            }

            @Override
            public void networkNotAvailable() {

            }

            @Override
            public void onErrorProceed(String s) {

            }

            @Override
            public void clientAuthenticationFailed(String s) {

            }

            @Override
            public void someUIErrorOccurred(String s) {

            }

            @Override
            public void onErrorLoadingWebPage(int i, String s, String s1) {

            }

            @Override
            public void onBackPressedCancelTransaction() {

            }

            @Override
            public void onTransactionCancel(String s, Bundle bundle) {

            }
        });
        transactionManager.setAppInvokeEnabled(true);
        transactionManager.setShowPaymentUrl("https://securegw-stage.paytm.in/theia/api/v1/showPaymentPage");
        transactionManager.startTransaction(this, 123);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 123 && data != null){
            Log.d("Result", data.toString());
        }
    }
}