/*
 * AMERICA ONLINE CONFIDENTIAL INFORMATION
 *
 * Copyright (c) 2017. AOL Inc
 * All Rights Reserved.  Unauthorized reproduction, transmission, or
 * distribution of this software is a violation of applicable laws.
 *
 */

HOW TO USE

1. Add tokenization-with-kount.aar file into libs folder of your Android project.

2. Add

allprojects {
    repositories {
        jcenter()
        flatDir {
            dirs 'libs'
        }
    }
}

into app level build.gradle file

3. Add

compile(name:'tokenization-with-kount', ext:'aar')

under dependencies of app level build.gradle file

4. Sync project

5. Add permission request for ACCESS_FINE_LOCATION for Android API >= 23

6. Use the following code to
  a) encrypt and tokenize a credit card number and cvv using Safetech:
    Tokenize tokenize = new Tokenize(context);
    String encryptedCreditCard = tokenize.creditCard(cardNumber, cardCvv, domain, merchantId);

  b) encrypt and tokenize a bank account number using Safetech:
    Tokenize tokenize = new Tokenize(activity);
    String encryptedBankAccount = tokenize.bankAccount(accountNumber, domain, merchantId);

Notes:
  All parameters are Strings except merchantId, which is int.
  Parameter domain shows the environment of the library usage and can be "DEV" or "PROD" or "QA".
  Any other value of domain will be considered as "QA".