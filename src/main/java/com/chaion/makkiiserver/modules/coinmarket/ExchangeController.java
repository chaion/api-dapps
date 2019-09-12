package com.chaion.makkiiserver.modules.coinmarket;

import com.chaion.makkiiserver.ServiceException;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Api(value="Exchange Market APIs", description="crypto currency, fiat currency exchange rates")
@RestController
public class ExchangeController {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeController.class);

    @Autowired
    private ExchangePool pool;

    @Autowired
    private CurrencyService currencyService;

    @ApiOperation(value="Get erc 20 price: erc20 -> fiat currency")
    @GetMapping(value="/market/erc20/price")
    public List<MarketPrice> getERC20TokenPrice(
            @ApiParam(value = "crypto currencies delimited by ','")
            @RequestParam(value = "crypto") String cryptos,
            @ApiParam(value = "fiat currency symbol")
            @RequestParam(value = "fiat") String fiat) {
        List<String> cryptoList = Arrays.asList(cryptos.split(","));
        try {
            Map<String, BigDecimal> prices = currencyService.fetchTokenPrice(cryptoList, fiat);
            List<MarketPrice> marketPrices = new ArrayList<>();
            for (Map.Entry<String, BigDecimal> entry : prices.entrySet()) {
                MarketPrice mp = new MarketPrice();
                mp.setCrypto(entry.getKey());
                mp.setFiat(fiat);
                mp.setPrice(entry.getValue());
                marketPrices.add(mp);
            }
            return marketPrices;
        } catch (ServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    cryptos + "<->" + fiat + " exchange rate not found.");
        }
    }

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
