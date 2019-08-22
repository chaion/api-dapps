package com.chaion.makkiiserver.pokket;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class PokketUtil {

    public static final String POKKET_WALLET_ADDRESS = "";
    public static final String MAKKII_WALLET_ADDRESS = "";
    public static final BigDecimal INCREASE_RATE = new BigDecimal(1.1);

    public static String generateOrderId() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return formatter.format(new Date()) + (new Random().nextInt(9999-1000+1) + 1000);
    }

    public static BigDecimal calculateCollateral(BigDecimal amount, BigDecimal rate, BigDecimal weeklyInterest) {
        return amount.multiply(INCREASE_RATE).add(amount.multiply(weeklyInterest)).multiply(rate);
    }

}
