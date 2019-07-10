package com.chaion.makkiiserver.controller;

import com.chaion.makkiiserver.model.MarketPrice;
import com.chaion.makkiiserver.services.exchange.ExchangePool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Api(value="Exchange Market APIs", description="crypto currency, fiat currency exchange rates")
@RestController
public class ExchangeController {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeController.class);

    @Autowired
    private ExchangePool pool;

    @ApiOperation(value="Get exchange rate: crypto currency->fiat currency")
    @GetMapping(value="/market/price")
    public MarketPrice getPrice(
            @ApiParam(value="crypto currency symbol")
            @RequestParam(value = "crypto") String crypto,
            @ApiParam(value="fiat currency symbol")
            @RequestParam(value = "fiat") String fiat) {
        logger.info("/market/price: crypto: " + crypto + ", fiat: " + fiat);
        return getPriceInternal(crypto, fiat);
    }

    @ApiOperation(value="refer to /market/price",
            notes = "same with /market/price api, legacy api to be compatible with app version 0.0.1")
    @GetMapping(value="/price")
    public MarketPrice getPriceOld(@ApiParam(value="crypto currency symbol")
            @RequestParam(value = "crypto") String crypto,
            @ApiParam(value="fiat currency symbol")
            @RequestParam(value = "fiat") String fiat) {
        logger.info("/price: crypto: " + crypto + ", fiat: " + fiat);
        return getPriceInternal(crypto, fiat);
    }

    private MarketPrice getPriceInternal(String crypto, String fiat) {
        BigDecimal price = pool.getPrice(crypto, fiat);
        if (price != null) {
            MarketPrice mp = new MarketPrice();
            mp.setCrypto(crypto);
            mp.setFiat(fiat);
            mp.setPrice(price);
            return mp;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                crypto + "<->" + fiat + " exchange rate not found.");
    }

    @ApiOperation(value="Get batches of exchange rates: crypto currencies -> fiat currencies")
    @GetMapping(value="/market/prices")
    public List<MarketPrice> getPrices(
            @ApiParam(value="crypto currency symbols delimited by ','")
            @RequestParam(value = "cryptos") String cryptoCurrencies,
            @ApiParam(value="fiat currency symbol")
            @RequestParam(value = "fiat") String fiat) {
        logger.info("/market/prices: cryptoCurrencies: " + cryptoCurrencies + ", fiat: " + fiat);
        List<MarketPrice> list = new ArrayList<>();
        String[] cryptos = cryptoCurrencies.split(",");
        for (String crypto: cryptos) {
            BigDecimal price = pool.getPrice(crypto, fiat);
            if (price != null) {
                MarketPrice mp = new MarketPrice();
                mp.setCrypto(crypto);
                mp.setFiat(fiat);
                mp.setPrice(price);
                list.add(mp);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                crypto + "<->" + fiat + " exchange rate not found.");
            }
        }
        return list;
    }
}
