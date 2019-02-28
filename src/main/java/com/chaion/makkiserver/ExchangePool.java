package com.chaion.makkiserver;

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

        fiatList.add("USD");
        fiatList.add("CAD");
        fiatList.add("CNY");
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
