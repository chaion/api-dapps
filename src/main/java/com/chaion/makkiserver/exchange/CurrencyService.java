package com.chaion.makkiserver.exchange;

import com.chaion.makkiserver.ServiceException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class CurrencyService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

    @Autowired
    RestTemplate rest;
    @Value("${marketcap_key}")
    String apiKey;

    public BigDecimal fetchCurrency(String cryptoCurrency, String fiatCurrency) throws ServiceException {
        logger.info("fetch exchange rate: crypto=" + cryptoCurrency + ", fiat=" + fiatCurrency);

        final HttpHeaders headers = new HttpHeaders();
        headers.set("X-CMC_PRO_API_KEY", apiKey);
        final HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest?symbol=" +
                cryptoCurrency + "&convert=" + fiatCurrency;

        ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET, entity, String.class);
        String body = resp.getBody();

        JsonObject root = new JsonParser().parse(body).getAsJsonObject();
        if (root.has("data")) {
            JsonObject data = root.get("data").getAsJsonObject();
            JsonObject crypto = data.get(cryptoCurrency).getAsJsonObject();
            JsonObject quote = crypto.get("quote").getAsJsonObject();
            JsonObject currency = quote.get(fiatCurrency).getAsJsonObject();
            BigDecimal price = currency.get("price").getAsBigDecimal();

            logger.info("fetch result: " + price);
            return price;
        } else {
            JsonObject status = root.get("status").getAsJsonObject();
            int errorCode = status.get("error_code").getAsInt();
            String errorMessage = status.get("error_message").getAsString();

            logger.info("fetch failed: errorCode=" + errorCode + ", errorMessage=" + errorMessage);
            throw new ServiceException(errorCode, errorMessage);
        }
    }
}
