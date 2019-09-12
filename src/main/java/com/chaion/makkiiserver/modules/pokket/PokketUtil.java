package com.chaion.makkiiserver.modules.pokket;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Utility methods for pokket business
 */
public class PokketUtil {

    public static final String POKKET_BTC_WALLET_ADDRESS = null;
    public static final String POKKET_ETH_WALLET_ADDRESS = null;
    public static final String MAKKII_WALLET_ADDRESS = null;
    /**
     * increase rate to determine whether return tusd.
     */
    public static final BigDecimal INCREASE_RATE = new BigDecimal(1.1);
    public static final String TUSD = "TUSD";
    public static final String BTC = "BTC";
    public static final String ETH = "ETH";

    public static final String ERROR_CODE_PRODUCT_EXPIRE = "pokket.product.expire";
    public static final String ERROR_CODE_EXCEED_QUOTA = "pokket.product.exceed_quota";

    /**
     * generate random order id for pokket purchase.
     * order id format is: <timestamp>+<random 5 digits>
     */
    public static String generateOrderId() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return formatter.format(new Date()) + (new Random().nextInt(9999-1000+1) + 1000);
    }

    /**
     * calculate collateral amount to be returned.
     *
     * @param amount
     * @param rate
     * @param weeklyInterest
     * @return
     */
    public static BigDecimal calculateCollateral(BigDecimal amount, BigDecimal rate, BigDecimal weeklyInterest) {
        return amount.multiply(INCREASE_RATE).add(amount.multiply(weeklyInterest)).multiply(rate);
    }

}
