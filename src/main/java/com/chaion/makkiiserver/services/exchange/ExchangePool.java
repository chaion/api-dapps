package com.chaion.makkiiserver.services.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExchangePool {

    private static final Logger logger = LoggerFactory.getLogger(ExchangePool.class);

    private List<String> cryptoList = new ArrayList<>();
    private List<String> fiatList = new ArrayList<>();
    private Map<String, BigDecimal> prices = new HashMap<>();

    public ExchangePool() {
        cryptoList.add("AION");
        cryptoList.add("BTC");
        cryptoList.add("ETH");
        cryptoList.add("EOS");
        cryptoList.add("LTC");
        cryptoList.add("TRX");

        fiatList.add("BTC");
        fiatList.add("USD");
        fiatList.add("ALL");
        fiatList.add("DZD");
        fiatList.add("ARS");
        fiatList.add("AMD");
        fiatList.add("AUD");
        fiatList.add("AZN");
        fiatList.add("BHD");
        fiatList.add("BDT");
        fiatList.add("BYN");
//        fiatList.add("BMD");
        fiatList.add("BOB");
        fiatList.add("BAM");
        fiatList.add("BRL");
        fiatList.add("BGN");
        fiatList.add("KHR");
        fiatList.add("CAD");
        fiatList.add("CLP");
        fiatList.add("CNY");
        fiatList.add("COP");
        fiatList.add("CRC");
        fiatList.add("HRK");
//        fiatList.add("CUP");
        fiatList.add("CZK");
        fiatList.add("DKK");
        fiatList.add("DOP");
        fiatList.add("EGP");
        fiatList.add("EUR");
        fiatList.add("GEL");
        fiatList.add("GHS");
        fiatList.add("GTQ");
        fiatList.add("HNL");
        fiatList.add("HKD");
        fiatList.add("HUF");
        fiatList.add("ISK");
        fiatList.add("INR");
        fiatList.add("IDR");
        fiatList.add("IRR");
        fiatList.add("IQD");
        fiatList.add("ILS");
        fiatList.add("JMD");
        fiatList.add("JPY");
        fiatList.add("JOD");
        fiatList.add("KZT");
        fiatList.add("KES");
        fiatList.add("KWD");
        fiatList.add("KGS");
        fiatList.add("LBP");
//        fiatList.add("MKD");
        fiatList.add("MYR");
        fiatList.add("MUR");
        fiatList.add("MXN");
        fiatList.add("MDL");
//        fiatList.add("MNT");
        fiatList.add("MAD");
        fiatList.add("MMK");
        fiatList.add("NAD");
        fiatList.add("NPR");
        fiatList.add("TWD");
        fiatList.add("NZD");
        fiatList.add("NIO");
        fiatList.add("NGN");
        fiatList.add("NOK");
        fiatList.add("OMR");
        fiatList.add("PKR");
        fiatList.add("PAB");
        fiatList.add("PEN");
        fiatList.add("PHP");
        fiatList.add("PLN");
        fiatList.add("GBP");
        fiatList.add("QAR");
        fiatList.add("RON");
        fiatList.add("RUB");
        fiatList.add("SAR");
        fiatList.add("RSD");
        fiatList.add("SGD");
        fiatList.add("ZAR");
        fiatList.add("KRW");
        fiatList.add("SSP");
        fiatList.add("VES");
        fiatList.add("LKR");
        fiatList.add("SEK");
        fiatList.add("CHF");
        fiatList.add("THB");
        fiatList.add("TTD");
        fiatList.add("TND");
        fiatList.add("TRY");
        fiatList.add("UGX");
        fiatList.add("UAH");
        fiatList.add("AED");
        fiatList.add("UYU");
        fiatList.add("UZS");
        fiatList.add("VND");
    }

    public void updatePrice(String crypto, String fiat, BigDecimal price) {
        prices.put(crypto + "->" + fiat, price);
    }

    public BigDecimal getPrice(String crypto, String fiat) {
        return prices.get(crypto + "->" + fiat);
    }

    public List<String> getCryptoList() {
        return cryptoList;
    }

    public List<String> getFiatList() {
        return fiatList;
    }

    public void dump() {
        for (Map.Entry<String, BigDecimal> entry : prices.entrySet()) {
            logger.info(entry.getKey() + ":" + entry.getValue());
        }
    }
}
