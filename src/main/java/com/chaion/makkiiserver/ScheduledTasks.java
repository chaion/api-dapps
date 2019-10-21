package com.chaion.makkiiserver;

import com.chaion.makkiiserver.blockchain.aion.AionService;
import com.chaion.makkiiserver.blockchain.btc.BtcService;
import com.chaion.makkiiserver.blockchain.eth.EthService;
import com.chaion.makkiiserver.modules.coinmarket.CurrencyService;
import com.chaion.makkiiserver.modules.news.CoinVoiceService;
import com.chaion.makkiiserver.modules.pokket.PokketService;
import com.chaion.makkiiserver.modules.token.EthTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private EthService ethService;

    @Autowired
    private BtcService btcService;

    @Autowired
    private AionService aionService;

    @Autowired
    private EthTokenRepository ethTokenRepo;

    @Autowired
    private PokketService pokketService;

    @Autowired
    private CoinVoiceService coinVoiceService;

    /**
     * Fetch currency rate every 30 minutes
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void fetchCurrencyRate() {
        try {
            currencyService.updateExchangeRates();
        } catch (ServiceException e) {
            logger.error("refresh exchange rates failed: ", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void refreshNews() {
        try {
            coinVoiceService.fetch();
        } catch (Exception e) {
            logger.error("fetch coin voice news failed: ", e.getMessage());
        }
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

    @Scheduled(fixedRate = 10 * 1000)
    public void checkPendingAionTxStatus() {
        aionService.checkPendingTxStatus();
    }

    @Scheduled(fixedRate = 10 * 1000)
    public void refreshPokketProductList() {
        pokketService.refreshProductList();
    }
}
