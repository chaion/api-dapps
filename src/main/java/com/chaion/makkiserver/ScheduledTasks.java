package com.chaion.makkiserver;

import com.chaion.makkiserver.exchange.CurrencyService;
import com.chaion.makkiserver.exchange.ExchangePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ExchangePool pool;

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void fetchCurrencyRate() {
        logger.info("fetch currency rate");

        for (String crypto : pool.getCryptoList()) {
            for (String currency : pool.getFiatList()) {
                try {
                    BigDecimal price = currencyService.fetchCurrency(crypto, currency);
                    pool.updatePrice(crypto, currency, price);
                } catch (ServiceException e) {
                    logger.error("fetch [" + crypto + "," + currency + "] failed: " + e.getMessage());
                }
            }
        }
        pool.dump();
    }
}
