package com.aol.obi.android.lib.kount;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.kount.api.DataCollector;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class Tokenize {

    private final String LOG_TAG = "myLogs: " + this.getClass().getSimpleName();

    private Context ctx;

    public Tokenize(Context ctx) {
        this.ctx = ctx;
    }

    public String creditCard(String cardNumber, String cardCvv, String domain, int merchantId) {

        if (!Validator.validate(cardNumber)){
            Toast.makeText(ctx, "Credit card number is not valid", Toast.LENGTH_SHORT).show();
            return "";
        }

        String sessionId = String.valueOf(UUID.randomUUID()).replace("-", "");
        setupDataCollector(sessionId, domain, merchantId);
        return setupEncryption("card", cardNumber, cardCvv, sessionId, domain);
    }

    public String bankAccount(String cardNumber, String domain, int merchantId) {
        String sessionId = String.valueOf(UUID.randomUUID()).replace("-", "");

        setupDataCollector(sessionId, domain, merchantId);

        return setupEncryption("bank", cardNumber, "", sessionId, domain);
    }

    private void setupDataCollector(final String sessionId, String domain, int merchantId) {
        DataCollector.getInstance().setContext(ctx);
        DataCollector.getInstance().setMerchantID(merchantId);
        DataCollector.getInstance().setLocationCollectorConfig(DataCollector.LocationConfig.COLLECT);

        if (domain.equals("PROD")) {
            // For Production Environment
            DataCollector.getInstance().setEnvironment(DataCollector.ENVIRONMENT_PRODUCTION);
        } else {
            // For Test Environment
            DataCollector.getInstance().setEnvironment(DataCollector.ENVIRONMENT_TEST);
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                DataCollector.getInstance().collectForSession(sessionId, new DataCollector.CompletionHandler() {
                    @Override
                    public void completed(String sessionID) {
                        Log.i(LOG_TAG, "Data collection completed.");
                        Toast.makeText(ctx, "Data collection completed.", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void failed(String sessionID, final DataCollector.Error error) {
                        Log.e(LOG_TAG, "Data collection failed. " + error.toString());
                        Toast.makeText(ctx, "Data collection failed. " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private String setupEncryption(String accountType, String number, String cvv, String sessionId, String domain) {

        String cardType = "";

        if (accountType.equals("card")) {
            cardType = getCardType(number);
        } else if (accountType.equals("bank")) {
            cardType = "CHECKING";
        }

        String encryptedCardToken = Encrypt.encrypt(number, cvv, domain);
        String fullResult = encryptedCardToken + ";" + sessionId + ";" + cardType;

        String encodedFullResult = "";

        try {
            byte[] fullResultByte = fullResult.getBytes("UTF-8");
            encodedFullResult = Base64.encodeToString(fullResultByte, Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encodedFullResult;
    }

    private static String getCardType(String cardNumber) {
        String cardType = "";

        if (cardNumber.length() > 2) {
            if (cardNumber.startsWith("4")) {
                cardType = "VISA";
            } else if ( cardNumber.startsWith("34") || cardNumber.startsWith("37") ) {
                cardType = "AMERICAN_EXPRESS";
            } else if ( (Integer.parseInt(cardNumber.substring(0, 3)) > 299 &&
                    Integer.parseInt(cardNumber.substring(0, 3)) < 306 ) ||
                    cardNumber.startsWith("36") || cardNumber.startsWith("38") ) {
                cardType = "DINERS CLUB";
            } else if ( cardNumber.startsWith("6011") || cardNumber.startsWith("65") ) {
                cardType = "DISCOVER";
            } else if ( cardNumber.startsWith("1800") || cardNumber.startsWith("2131") ||
                    cardNumber.startsWith("35")) {
                cardType = "JCB";
            } else {
                cardType = "MASTER_CARD";
            }
        }

        return cardType;
    }
}