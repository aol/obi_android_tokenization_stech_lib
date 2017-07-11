/*
 * AMERICA ONLINE CONFIDENTIAL INFORMATION
 *
 * Copyright (c) 2017. AOL Inc
 * All Rights Reserved.  Unauthorized reproduction, transmission, or
 * distribution of this software is a violation of applicable laws.
 *
 * Use the following code to encrypt a credit card and tokenize the payment method.
 * String encryptedCardToken = EncryptCard.encryptCard(cardNumber, cardCvv, domain);
 *
 */

package com.aol.obi.android.lib.tokenization;

import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encrypt {

    /**
     *
     * Usage:
     *
     * String encryptedCard = Encrypt.encrypt(cardNumber, cardCvv, domain);
     *
     * or
     *
     * String encryptedBankAccount = Encrypt.encrypt(accountNumber, "", domain);
     *
     *
     * @param cardNumber - String value of credit card number. Can be with spaces, with dashes, with dots or without.
     * @param cardCvv - String value of credit card cvv or empty string in case of bank account.
     * @param domain - String showing the environment of the library usage and can be "DEV" or "PROD" or "QA".
     *                 Any other value of domain will be considered as "QA".
     * @return - Returns encrypted data string.
     * @throws OBISystemException - Throws OBISystemException if system error occured. user need to retry.
     */

    public static String encrypt(String cardNumber, String cardCvv, String domain) throws OBISystemException {

        String reqToken = String.valueOf( UUID.randomUUID()).replace("-", "");

        cardNumber = cardNumber.replace("-", "");
        cardNumber = cardNumber.replace(" ", "");
        cardNumber = cardNumber.replace(".", "");

        String cardNumberWithCvv = cardNumber + ";" + cardCvv;
        String encodedCardNumber = Encrypt.encrypt(cardNumberWithCvv, reqToken);

        TokenizePaymentMethod tokenizePaymentMethod = new TokenizePaymentMethod();

        String urlToTokenizePaymentMethod;

        switch (domain) {
            case "DEV":
                urlToTokenizePaymentMethod = "https://jsl.dev.obi.aol.com/obipmservice/apiCall?" +
                        "apiName=tokenizePaymentMethod&country=US&lang=en";
                break;

            case "PROD":
                urlToTokenizePaymentMethod = "https://jsl.prod.obi.aol.com/obipmservice/apiCall?" +
                        "apiName=tokenizePaymentMethod&country=US&lang=en";
                break;

            case "QA":
            default:
                urlToTokenizePaymentMethod = "https://jsl.qat.obi.aol.com/obipmservice/apiCall?" +
                        "apiName=tokenizePaymentMethod&country=US&lang=en";
                break;
        }

        try {
            String tokenizedPaymentMethod = tokenizePaymentMethod
                    .execute(encodedCardNumber, urlToTokenizePaymentMethod)
                    .get();

            if (tokenizedPaymentMethod != null) {
                try {
                    JSONObject jObjectTokenizedPaymentMethod = new JSONObject(tokenizedPaymentMethod);

                    JSONObject oneObjectData = jObjectTokenizedPaymentMethod.getJSONObject("data");
                    JSONObject oneObjectResponse = oneObjectData.getJSONObject("m:tokenizePaymentMethodResponse");
                    return oneObjectResponse.getString("m:result")+";"+reqToken;

                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new OBISystemException("System exception. please retry");
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new OBISystemException("System exception. please retry");
        }

        return "";
    }

    private static String encrypt(String cardNumber, String reqToken) {
        String encodedCardNumber = null;
        try {
            Cipher cipher = Cipher.getInstance("AES");

            byte[] cardNumberByte = getPaddedByte(cardNumber.getBytes());
            byte[] reqTokenByte = reqToken.getBytes();

            SecretKeySpec symKey = new SecretKeySpec(reqTokenByte, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, symKey);
            byte[] encryptedByte = cipher.doFinal(cardNumberByte);

            encodedCardNumber = Base64.encodeToString(encryptedByte, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodedCardNumber;
    }

    // this function ensures that last block is 16bytes and
    // unused bytes initialed to zero
    private static byte[] getPaddedByte(byte[] inp) {
        int remainder = 16 - (inp.length % 16);
        byte[] in = new byte[inp.length + remainder];
        for (int i = 0; i < in.length; i++) {
            if (i < inp.length) {
                in[i] = inp[i];
            } else {
                in[i] = 0;
            }
        }
        return in;
    }

    private static class TokenizePaymentMethod extends AsyncTask<String, Void, String > {

        @Override
        protected String doInBackground(String... params) {

            String encodedCardNumber = params[0].trim();

            String urlToTokenizePaymentMethod = params[1];

            try {
                URL url = new URL(urlToTokenizePaymentMethod);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(10000);
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-type","application/json; charset=UTF-8");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();

                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                String postBody = "{\"tokenizePaymentMethod\":{\"requestData\":\"" + encodedCardNumber + "\"}}";

                bufferedWriter.write(postBody);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                int responseCode = httpURLConnection.getResponseCode();

                InputStream inputStream;

                if (responseCode >= 400) {
                    inputStream = httpURLConnection.getErrorStream();
                } else {
                    inputStream = httpURLConnection.getInputStream();
                }

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return stringBuilder.toString().trim();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
