/*
 * AMERICA ONLINE CONFIDENTIAL INFORMATION
 *
 * Copyright (c) 2017. AOL Inc
 * All Rights Reserved.  Unauthorized reproduction, transmission, or
 * distribution of this software is a violation of applicable laws.
 *
 */

HOW TO USE

1. Add
compile 'com.aol.obi:android-tokenization-stech-lib-lib:1.0'

under dependencies of app level build.gradle file

2. Sync project

3. Add permission request for ACCESS_FINE_LOCATION for Android API >= 23

4. Use the following code

  a) to validate, encrypt and tokenize a credit card number and cvv using lib:

     Tokenize tokenize = new Tokenize(context);
     String encryptedCreditCard = tokenize.creditCard(cardNumber, cardCvv, domain, merchantId);

  b) to encrypt and tokenize a bank account number using Kount:

     Tokenize tokenize = new Tokenize(context);
     String encryptedBankAccount = tokenize.bankAccount(accountNumber, domain, merchantId);

  c) to only encrypt a credit card number:

     String encryptedCard = Encrypt.encrypt(cardNumber, cardCvv, domain);

  d) to only encrypt a bank account number:

     String encryptedBankAccount = Encrypt.encrypt(accountNumber, "", domain);

  e) to validate a credit card

     boolean isValid = Validator.validate(cardNumber);
     
  f) Exceptions: 

     OBIValidationException -> Throws this exception when invalid card passed. User need to correct the input
     OBISystemException -> Throws this exception incase of unknown system errors, user can retry.
  

___________
Notes:
  All parameters are Strings except merchantId, which is int.
  Parameter domain shows the environment of the library usage and can be "DEV" or "PROD" or "QA".
  Any other value of domain will be considered as "QA".
