package com.chaion.makkiiserver.modules.pokket;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Utility methods for pokket business
 */
public class PokketUtil {

    public static final String POKKET_BTC_WALLET_ADDRESS = null;
    public static final String POKKET_ETH_WALLET_ADDRESS = null;

    public static final String KEY_ORDER_ID = "order_id";
    public static final String KEY_TX_FOR = "tx_for";
    public static final String TX_FOR_INVESTMENT = "invest";
    public static final String TX_FOR_COLLATERAL_SETTLEMENT = "collateralSettlement";
    public static final String TX_FOR_COMPLETE = "complete";
    public static final String KEY_PRODUCT_ID = "product_id";
    public static final String KEY_INVESTMENT_ADDRESS = "investment_address";
    public static final String KEY_COLLATERAL_ADDRESS = "collateral_address";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_NEW_ORDERS = "new_orders";
    public static final String KEY_TO_DEPOSIT_TUSD = "is_deposit_tusd";
    public static final String KEY_CURRENT_TIME = "current_time";
    public static final String KEY_RESULT = "result";
    public static final String KEY_TX_HASH_RETURN_TUSD = "tx_hash_return_tusd";

    /**
     * increase rate to determine whether return tusd.
     */
    public static final BigDecimal INCREASE_RATE = new BigDecimal("1.1");
    public static final String TUSD = "TUSD";
    public static final String BTC = "BTC";
    public static final String ETH = "ETH";

    public static final String ERROR_CODE_PRODUCT_EXPIRE = "pokket.product_list.expire";
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
        return amount.multiply(INCREASE_RATE)
                .add(amount.multiply(weeklyInterest.divide(new BigDecimal("100"))))
                .multiply(rate)
                .setScale(2, RoundingMode.FLOOR);
    }

    /**
     * calculate token profit
     *
     * @param amount
     * @param weeklyInterest
     * @return
     */
    public static BigDecimal calculateProfit(BigDecimal amount, BigDecimal weeklyInterest, Integer allowedInteger) {
        return amount.multiply(weeklyInterest.divide(new BigDecimal("100"))
                .add(new BigDecimal("1")))
                .setScale(allowedInteger, RoundingMode.FLOOR);
    }

}
