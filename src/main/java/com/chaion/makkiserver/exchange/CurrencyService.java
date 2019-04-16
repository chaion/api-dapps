package com.chaion.makkiserver.exchange;

import com.chaion.makkiserver.ServiceException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


@Service
public class CurrencyService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

    @Autowired
    RestTemplate rest;

    public Map<String,BigDecimal> fetchCurrency(String cryptoCurrency, List<String> fiatCurrencys) throws ServiceException {
        logger.info("fetch exchange rate: crypto=" + cryptoCurrency + ", fiat=" + String.join(",",fiatCurrencys));

        Map<String,BigDecimal> prices = new HashMap<>();

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
                JsonObject crypto = raw.get(cryptoCurrency).getAsJsonObject();
                for (String fiatCurrency : targetCurrencies) {
                    if (crypto.has(fiatCurrency)) {
                        JsonObject currency = crypto.get(fiatCurrency).getAsJsonObject();
                        BigDecimal price = currency.get("PRICE").getAsBigDecimal();
                        prices.put(fiatCurrency, price);
                    } else {
                        logger.info(fiatCurrency + " is unsupported");
                    }
                }
            } else {
                int errorType = root.get("Type").getAsInt();
                String errorMessage = root.get("Message").getAsString();

                logger.info("fetch failed: errorType=" + errorType + ", errorMessage=" + errorMessage);
                throw new ServiceException(errorType, errorMessage);
            }
        }
        logger.info("fetch result:" + prices.toString());
        return prices;
    }
}
