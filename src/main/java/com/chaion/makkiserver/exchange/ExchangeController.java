package com.chaion.makkiserver.exchange;

import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class ExchangeController {
    @Autowired
    private ExchangePool pool;

    @RequestMapping(value="/price", method= RequestMethod.GET)
    public String getPrice(@RequestParam(value = "crypto") String symbol,
                           @RequestParam(value = "fiat") String fiat) {
        BigDecimal price = pool.getPrice(symbol, fiat);
        if (price != null) {
            JsonObject data = new JsonObject();
            data.addProperty("crypto", symbol);
            data.addProperty("fiat", fiat);
            data.addProperty("price", price);
            return data.toString();
        } else {
            return "{\"error\": \"no price found\"}";
        }
    }
}
