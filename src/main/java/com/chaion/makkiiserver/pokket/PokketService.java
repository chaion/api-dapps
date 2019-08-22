package com.chaion.makkiiserver.pokket;

import com.chaion.makkiiserver.pokket.model.PokketProduct;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PokketService {
    private static final Logger logger = LoggerFactory.getLogger(PokketService.class);

    public static final String ADDRESS_TYPE_ETH = "ERC20";
    public static final String ADDRESS_TYPE_BITCOIN = "Bitcoin";

    @Autowired
    RestTemplate restClient;

    @Value("${pokket.server}")
    String baseUrl;

    public void createOrder(String orderId,
                                   String investorAddress,
                                   String collateralAddress,
                                   String token,
                                   BigDecimal amount,
                                   Long orderTime,
                                   Boolean autoRoll,
                                   String depositTransactionHash,
                                   String previousOrderId) {
        logger.info("pokket service: create order");
    }

    public void setAutoRoll(String orderId, boolean autoroll) {
        logger.info("pokket service: set autoroll");
    }

    public List<PokketProduct> searchProducts(String search) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> map = new HashMap<>();
        map.put("token_name", search);
        HttpEntity request = new HttpEntity(map, headers);

        String url = baseUrl + "/products/search";
        ResponseEntity<String> response = restClient.postForEntity(url, request, String.class);
        if (response.getStatusCodeValue() == 200) {
            return parseToProductList(response.getBody());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, url);
        }
    }

    public List<PokketProduct> getProducts() {
        String url = baseUrl + "/products/list";
        ResponseEntity<String> response = restClient.getForEntity(url, String.class);
        if (response.getStatusCodeValue() == 200) {
            return parseToProductList(response.getBody());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, url);
        }
    }

    private List<PokketProduct> parseToProductList(String response) {
        List<PokketProduct> allProducts = new ArrayList<>();
        JsonArray array = new JsonParser().parse(response).getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            JsonObject productJo = array.get(i).getAsJsonObject();
            PokketProduct product = new PokketProduct();
            product.setProductId(productJo.get("product_id").getAsLong());
            product.setToken(productJo.get("token").getAsString());
            product.setTokenFullName(productJo.get("full_name").getAsString());
            product.setWeeklyInterestRate(productJo.get("weekly_percentage").getAsBigDecimal());
            product.setYearlyInterestRate(productJo.get("annual_percentage").getAsBigDecimal());
            product.setRemainingQuota(productJo.get("quota").getAsBigDecimal());
            product.setMinInvestAmount(productJo.get("minimum_order").getAsBigDecimal());
            product.setToken2Collateral(productJo.get("exchange_rate").getAsBigDecimal());
            allProducts.add(product);
        }
        return allProducts;
    }

    public BigDecimal getTotalInvestment() {
        String url = baseUrl + "/deposit/total_amount";
        ResponseEntity<String> response = restClient.getForEntity(url, String.class);
        if (response.getStatusCodeValue() == 200) {
            JsonObject obj = new JsonParser().parse(response.getBody()).getAsJsonObject();
            return obj.get("total_deposit_amount").getAsBigDecimal();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, url);
        }
    }

    public String getDepositAddress(String type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> map = new HashMap<>();
        map.put("type", type);
        HttpEntity request = new HttpEntity(map, headers);

        String url = baseUrl + "/deposit/address";
        ResponseEntity<String> response = restClient.postForEntity(url, request, String.class);
        if (response.getStatusCodeValue() == 200) {
            JsonObject obj = new JsonParser().parse(response.getBody()).getAsJsonObject();
            return obj.get("deposit_address").getAsString();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, url);
        }
    }
}
