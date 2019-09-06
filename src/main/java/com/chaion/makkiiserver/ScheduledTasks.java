package com.chaion.makkiiserver;

import com.chaion.makkiiserver.blockchain.btc.BtcService;
import com.chaion.makkiiserver.blockchain.eth.EthService;
import com.chaion.makkiiserver.modules.coinmarket.CurrencyService;
import com.chaion.makkiiserver.modules.coinmarket.ExchangePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ExchangePool pool;

    @Autowired
    private EthService ethService;

    @Autowired
    private BtcService btcService;

    /**
     * Fetch currency rate every 30 minutes
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void fetchCurrencyRate() {
        logger.info("fetch currency rate");

        for (String crypto : pool.getCryptoList()) {
            try{
                Map<String,BigDecimal> prices = currencyService.fetchCurrency(crypto, pool.getFiatList());
                for(String currency: prices.keySet()){
                    pool.updatePrice(crypto, currency, prices.get(currency));
                }
            }catch (ServiceException e) {
                logger.error("fetch [" + crypto +  "] failed: " + e.getMessage());
            }
        }
        pool.dump();
    }

    /**
     * check pending transaction status every 10 seconds
     */
    @Scheduled(fixedRate = 10 * 1000)
    public void checkPendingEthTxStatus() {
        ethService.checkPendingTxStatus();
    }

    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void checkPendingBtcTxStatus() {
        btcService.checkPendingTxStatus();
    }
}
