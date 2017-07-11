package com.aol.obi.android.lib.tokenization;

public class Validator {

    private static final byte VISA = 0;
    private static final byte MASTERCARD = 1;
    private static final byte AMEX = 2;
    private static final byte DINERS_CLUB = 3;
    private static final byte CARTE_BLANCHE = 4;
    private static final byte DISCOVER = 5;
    private static final byte ENROUTE = 6;
    private static final byte JCB = 7;

    @SuppressWarnings("unused")
    public static boolean validate(final String credCardNumber, final byte type) {
        String creditCard = credCardNumber.trim();
        boolean applyAlgorithm = false;
        switch (type) {
            case VISA:
                // VISA credit cards has length 13 - 15
                // VISA credit cards starts with prefix 4
                if (creditCard.length() >= 13 && creditCard.length() <= 16
                        && creditCard.startsWith("4")) {
                    applyAlgorithm = true;
                }
                break;
            case MASTERCARD:
                // MASTERCARD has length 16
                // MASTER card starts with 51, 52, 53, 54 or 55
                if (creditCard.length() == 16) {
                    int prefix = Integer.parseInt(creditCard.substring(0, 2));
                    if (prefix >= 51 && prefix <= 55) {
                        applyAlgorithm = true;
                    }
                }
                break;
            case AMEX:
                // AMEX has length 15
                // AMEX has prefix 34 or 37
                if (creditCard.length() == 15
                        && (creditCard.startsWith("34") || creditCard
                        .startsWith("37"))) {
                    applyAlgorithm = true;
                }
                break;
            case DINERS_CLUB:
            case CARTE_BLANCHE:
                // DINERSCLUB or CARTEBLANCHE has length 14
                // DINERSCLUB or CARTEBLANCHE has prefix 300, 301, 302, 303, 304,
                // 305 36 or 38
                if (creditCard.length() == 14) {
                    int prefix = Integer.parseInt(creditCard.substring(0, 3));
                    if ((prefix >= 300 && prefix <= 305)
                            || creditCard.startsWith("36")
                            || creditCard.startsWith("38")) {
                        applyAlgorithm = true;
                    }
                }
                break;
            case DISCOVER:
                // DISCOVER card has length of 16
                // DISCOVER card starts with 6011
                if (creditCard.length() == 16 && creditCard.startsWith("6011")) {
                    applyAlgorithm = true;
                }
                break;
            case ENROUTE:
                // ENROUTE card has length of 16
                // ENROUTE card starts with 2014 or 2149
                if (creditCard.length() == 16
                        && (creditCard.startsWith("2014") || creditCard
                        .startsWith("2149"))) {
                    applyAlgorithm = true;
                }
                break;
            case JCB:
                // JCB card has length of 16 or 15
                // JCB card with length 16 starts with 3
                // JCB card with length 15 starts with 2131 or 1800
                if ((creditCard.length() == 16 && creditCard.startsWith("3"))
                        || (creditCard.length() == 15 && (creditCard
                        .startsWith("2131") || creditCard
                        .startsWith("1800")))) {
                    applyAlgorithm = true;
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
        return applyAlgorithm && validate(creditCard);
    }

    /**
     *
     * Usage:
     *
     * boolean isValid = Validator.validate(cardNumber);
     *
     *
     * @param cardNumber - String value of credit card number. Can be with spaces, with dashes, with dots or without.
     * @return - true or false
     */

    public static boolean validate(String cardNumber) {
        cardNumber = cardNumber.replace("-", "");
        cardNumber = cardNumber.replace(" ", "");
        cardNumber = cardNumber.replace(".", "");

        // 4 9 9 2 7 3 9 8 7 1 6
        // 6
        // 1 x 2 = 2  = (0 + 2) = 2
        // 7
        // 8 x 2 = 16 = (1 + 6) = 7
        // 9
        // 3 x 2 = 6 = (0 + 6) = 6
        // 7
        // 2 x 2 = 4 = (0 + 4) = 4
        // 9
        // 9 X 2 = 18 = (1 + 8) = 9
        // 4
        // 6+2+7+7+9+6+7+4+9+9+4 = 70
        // return 0 == (70 % 10)

        int sum = 0;
        int length = cardNumber.length();
        for (int i = 0; i < cardNumber.length(); i++) {
            if (0 == (i % 2)) {
                sum += cardNumber.charAt(length - i - 1) - '0';
            } else {
                sum += sumDigits((cardNumber.charAt(length - i - 1) - '0') * 2);
            }
        }
        return 0 == (sum % 10) && cardNumber.length() >= 13;
    }

    private static int sumDigits(int i) {
        return (i % 10) + (i / 10);
    }
}