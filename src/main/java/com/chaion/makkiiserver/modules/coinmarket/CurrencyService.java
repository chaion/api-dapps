package com.chaion.makkiiserver.modules.coinmarket;

import com.chaion.makkiiserver.ServiceException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


@Service
public class CurrencyService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

    @Autowired
    RestTemplate rest;

    @Autowired
    RedisTemplate redisTemplate;

    private List<String> cryptoList = new ArrayList<>();
    private List<String> fiatList = new ArrayList<>();

    public CurrencyService() {
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
//        fiatList.add("AZN");
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

    public void updateExchangeRates() throws ServiceException {
        logger.info("fetch currency rate");
        for (String crypto : this.cryptoList) {
            fetchCurrency(crypto, this.fiatList);
        }
    }

    public Map<String, BigDecimal> fetchTokenPrice(List<String> tokens, String fiatCurrency) throws ServiceException {
        logger.info("fetch token price: erc20=" + String.join(",", tokens) + ", fiat=" + fiatCurrency);
        Map<String, BigDecimal> prices = new HashMap<>();

        final HttpHeaders headers = new HttpHeaders();
        final HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=" + String.join(",", tokens)
                + "&tsyms=" + fiatCurrency;

        logger.info("fetch url: " + url);
        ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET, entity, String.class);
        String body = resp.getBody();

        JsonObject root = new JsonParser().parse(body).getAsJsonObject();
        if (root.has("RAW")) {
            JsonObject raw = root.get("RAW").getAsJsonObject();
            for (String token : tokens) {
                if (raw.has(token)) {
                    JsonObject tokenjo = raw.get(token).getAsJsonObject();
                    if (tokenjo.has(fiatCurrency)) {
                        JsonObject currency = tokenjo.get(fiatCurrency).getAsJsonObject();
                        BigDecimal price = currency.get("PRICE").getAsBigDecimal();
                        prices.put(token, price);
                    } else {
                        logger.info(token + "->" + fiatCurrency + " is not supported");
                    }
                } else {
                    logger.info(token + "->" + fiatCurrency + " is not supported");
                }
            }
        } else {
            int errorType = root.get("Type").getAsInt();
            String errorMessage = root.get("Message").getAsString();

            logger.info("fetch failed: errorType=" + errorType + ", errorMessage=" + errorMessage);
            throw new ServiceException(errorType, errorMessage);
        }

        return prices;
    }

    public void fetchCurrency(String cryptoCurrency, List<String> fiatCurrencys) throws ServiceException {
        logger.info("fetch exchange rate: crypto=" + cryptoCurrency + ", fiat=" + String.join(",",fiatCurrencys));

        final HttpHeaders headers = new HttpHeaders();
        final HttpEntity<String> entity = new HttpEntity<>(headers);

        int endIndex = 0;

        while (endIndex < fiatCurrencys.size()) {
            List<String> targetCurrencies = fiatCurrencys.subList(endIndex, Math.min(endIndex + 20, fiatCurrencys.size()));
            String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=" +
                    cryptoCurrency + "&tsyms=" + String.join(",", targetCurrencies);
            endIndex += 20;

            logger.info("fetch url: " + url);
            ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET, entity, String.class);
            String body = resp.getBody();

            JsonObject root = new JsonParser().parse(body).getAsJsonObject();
            if (root.has("RAW")) {
                JsonObject raw = root.get("RAW").getAsJsonObject();
                if (raw.has(cryptoCurrency)) {
                    JsonObject crypto = raw.get(cryptoCurrency).getAsJsonObject();
                    for (String fiatCurrency : targetCurrencies) {
                        if (crypto.has(fiatCurrency)) {
                            JsonObject currency = crypto.get(fiatCurrency).getAsJsonObject();
                            BigDecimal price = currency.get("PRICE").getAsBigDecimal();
                            redisTemplate.opsForValue().set(cryptoCurrency + "->" + fiatCurrency, price);
                        } else {
                            logger.info(fiatCurrency + " is unsupported");
                        }
                    }
                } else {
                    logger.info(cryptoCurrency + " is unsupported");
                }
            } else {
                int errorType = root.get("Type").getAsInt();
                String errorMessage = root.get("Message").getAsString();

                logger.info("fetch failed: errorType=" + errorType + ", errorMessage=" + errorMessage);
                throw new ServiceException(errorType, errorMessage);
            }
        }
    }
}
