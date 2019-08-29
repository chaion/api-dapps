package com.chaion.makkiiserver.modules.pokket;

import com.chaion.makkiiserver.modules.pokket.model.PokketProduct;
import com.chaion.makkiiserver.modules.pokket.security.AesProvider;
import com.chaion.makkiiserver.modules.pokket.security.CipherHelper;
import com.chaion.makkiiserver.modules.pokket.security.RsaProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PokketService {
    private static final Logger logger = LoggerFactory.getLogger(PokketService.class);

    public static final String ADDRESS_TYPE_ETH = "ERC20";
    public static final String ADDRESS_TYPE_BITCOIN = "Bitcoin";

    @Autowired
    RestTemplate restClient;

    @Value("${pokket.server}")
    String baseUrl;

    private RsaProvider rsaProvider;

    public PokketService() throws IOException {
        rsaProvider = new RsaProvider();
        ClassPathResource classPathResource = new ClassPathResource("pokket_public.pem");
        InputStream stream = classPathResource.getInputStream();
        rsaProvider.loadPemFile(stream);
    }

    public Long createOrder(String orderId,
                                   Long productId,
                                   String investorAddress,
                                   String collateralAddress,
                                   BigDecimal amount,
                                   Long orderTime,
                                   String depositTransactionHash
                                   ) {
        logger.info("pokket service: create order");
        JsonObject order = new JsonObject();
        order.addProperty("product_id", productId);
        order.addProperty("amount", amount);
        if (collateralAddress != null) {
            order.addProperty("tusd_address", collateralAddress);
        }
        order.addProperty("return_address", investorAddress);
        order.addProperty("transaction_id", depositTransactionHash);
        String createTime = new Date(orderTime).toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        order.addProperty("create_time", createTime);
        order.addProperty("order_id", orderId);

        logger.info("order json:" + order.toString());

        String keyHash = null;
        String depositCipher = null;
        try {
            AesProvider aesProvider = new AesProvider();
            String keyString = aesProvider.createKey();
            depositCipher = CipherHelper.toString(aesProvider.encrypt(order.toString()));
            keyHash = CipherHelper.toString(rsaProvider.encrypt(keyString));
            logger.info("keyString:" + keyString);
            logger.info("depositCipher:" + depositCipher);
            logger.info("keyHash:" + keyHash);
        } catch (Exception e) {
            logger.error("encrypt/cipher order fail", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "encrypt order failed.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> map = new HashMap<>();
        map.put("deposit_cipher", depositCipher);
        map.put("key_hash", keyHash);
        HttpEntity request = new HttpEntity(map, headers);

        String url = baseUrl + "/deposit/deposit";
        ResponseEntity<String> response;
        try {
             response = restClient.postForEntity(url, request, String.class);
        } catch (Exception e) {
            logger.info("response: " + e.getMessage());
            throw e;
        }
        logger.info(response.toString());
        if (response.getStatusCodeValue() != 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, url);
        } else {
            try {
                return Long.parseLong(response.getBody());
            } catch (Exception e) {
                // if
                return -1l;
            }
        }
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
