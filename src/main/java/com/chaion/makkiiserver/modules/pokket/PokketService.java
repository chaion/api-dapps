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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This service is the stub client of pokket server side.
 *
 * https://aion.baibaitesting.com/doc/index.html
 */
@Service
public class PokketService {

    private static final Logger logger = LoggerFactory.getLogger(PokketService.class);

    public static final String ADDRESS_TYPE_ETH = "ERC20";
    public static final String ADDRESS_TYPE_BITCOIN = "Bitcoin";

    @Autowired
    RestTemplate restClient;

    private List<PokketProduct> cachedProductList = Collections.synchronizedList(new ArrayList<>());

    /**
     * pokket server base url
     */
    String baseUrl;

    private RsaProvider rsaProvider;

    public PokketService(@Value("${pokket.server.pubkey}") String pkFileName,
                         @Value("${pokket.server.baseurl}") String baseUrl) throws IOException {
        logger.info("initialize pokket service: baseurl=" + baseUrl + ", pkFileName=" + pkFileName);
        this.baseUrl = baseUrl;

        rsaProvider = new RsaProvider();
        ClassPathResource classPathResource = new ClassPathResource(pkFileName);
        InputStream stream = classPathResource.getInputStream();
        rsaProvider.loadPemFile(stream);
    }

    /**
     * after investment transaction is confirmed, notify pokket server of this purchase order.
     *
     * @param orderId
     * @param productId
     * @param investorAddress
     * @param collateralAddress
     * @param amount
     * @param orderTime
     * @param depositTransactionHash
     * @return
     */
    public Long createOrder(String orderId,
                                   Long productId,
                                   String investorAddress,
                                   String collateralAddress,
                                   BigDecimal amount,
                                   Long orderTime,
                                   String depositTransactionHash
                                   ) throws PokketServiceException {
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
            logger.error("encrypt/cipher order fail" + e.getMessage());
            logger.debug("encrypt/cipher order fail:", e);
            throw new PokketServiceException("[pokket] encrypt order failed: " + e.getMessage());
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
            logger.info("calling pokket " + url);

            response = restClient.postForEntity(url, request, String.class);
            logger.info(response.toString());
            try {
                return new JsonParser().parse(response.getBody()).getAsJsonObject().get("order_id").getAsLong();
            } catch (Exception e) {
                logger.error("parse response exception:" + e.getMessage());
                logger.debug("parse response exception: ", e);
                throw new PokketServiceException("[pokket] parse order id failed: " + e.getMessage());
            }
        } catch (HttpStatusCodeException e) {
            logger.error("create order exception: code:" + e.getRawStatusCode()
                    + ",response text:" + e.getResponseBodyAsString());
            throw new PokketServiceException("[pokket] deposit failed: " + e.getResponseBodyAsString(),
                    e.getRawStatusCode());
        }
    }

    /**
     * Client won't call this interface any more, client will filter locally.
     *
     * @param search
     * @return
     */
    @Deprecated
    public List<PokketProduct> searchProducts(String search) throws PokketServiceException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> map = new HashMap<>();
        map.put("token_name", search);
        HttpEntity request = new HttpEntity(map, headers);

        String url = baseUrl + "/products/search";
        try {
            logger.info("calling " + url);
            ResponseEntity<String> response = restClient.postForEntity(url, request, String.class);
            return parseToProductList(response.getBody());
        } catch (HttpStatusCodeException e) {
            logger.error("search products exception: code:" + e.getRawStatusCode()
                    + ",response text:" + e.getResponseBodyAsString());
            throw new PokketServiceException("[pokket] search products: " + e.getResponseBodyAsString(),
                    e.getRawStatusCode());
        }
    }

    /**
     * Get current product list
     */
    public List<PokketProduct> getProducts() throws PokketServiceException {
        String url = baseUrl + "/products/list";
        try {
//            logger.info("calling " + url);
            ResponseEntity<String> response = restClient.getForEntity(url, String.class);
            return parseToProductList(response.getBody());
        } catch (HttpStatusCodeException e) {
            logger.error("get products exception: code:" + e.getRawStatusCode()
                    + ",response text:" + e.getResponseBodyAsString());
            throw new PokketServiceException("[pokket] get products: "+ e.getResponseBodyAsString(),
                    e.getRawStatusCode());
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

    /**
     * Get total pokket investment amount
     *
     * @return
     */
    public BigDecimal getTotalInvestment() throws PokketServiceException {
        String url = baseUrl + "/deposit/total_amount";
        try {
            logger.info("calling " + url);
            ResponseEntity<String> response = restClient.getForEntity(url, String.class);
            JsonObject obj = new JsonParser().parse(response.getBody()).getAsJsonObject();
            return obj.get("total_deposit_amount").getAsBigDecimal();
        } catch (HttpStatusCodeException e) {
            logger.error("get total investment exception: code:" + e.getRawStatusCode()
                    + ",response text:" + e.getResponseBodyAsString());
            throw new PokketServiceException("[pokket] get total investment: " + e.getResponseBodyAsString(),
                    e.getRawStatusCode());
        }
    }

    /**
     * Get what investorAddresses investors should transfer coins/tokens to.
     *
     * @param type either ERC20 or Bitcoin
     * @return
     */
    public String getDepositAddress(String type) throws PokketServiceException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> map = new HashMap<>();
        map.put("type", type);
        HttpEntity request = new HttpEntity(map, headers);

        String url = baseUrl + "/deposit/address";
        try {
            logger.info("calling " + url);
            ResponseEntity<String> response = restClient.postForEntity(url, request, String.class);
            JsonObject obj = new JsonParser().parse(response.getBody()).getAsJsonObject();
            String depositAddress = obj.get("deposit_address").getAsString();
            String signature = obj.get("signature").getAsString();
            try {
                if (rsaProvider.verify(depositAddress, signature)) {
                    return depositAddress;
                } else {
                    throw new PokketServiceException("[pokket] deposit address responds with invalid signature.");
                }
            } catch (GeneralSecurityException e) {
                logger.error("verify signature failed: " + e.getMessage());
                throw new PokketServiceException("[pokket] deposit address failed to verify signature:" + e.getMessage());
            }
        } catch (HttpStatusCodeException e) {
            logger.error("get deposit address exception: code:" + e.getRawStatusCode()
                    + ",response text:" + e.getResponseBodyAsString());
            throw new PokketServiceException("[pokket] get deposit address: " + e.getResponseBodyAsString(),
                    e.getRawStatusCode());
        }
    }

    /**
     * Load pokket product list and save into cache.
     */
    public void refreshProductList() {
//        logger.info("refresh pokket product list...");
        try {
            List<PokketProduct> products = getProducts();
            cachedProductList.clear();
            cachedProductList.addAll(products);
        } catch (Exception e) {
            logger.error("refresh product list exception: " + e.getMessage());
        }
    }

    /**
     * Get cached product list.
     *
     * @return
     */
    public List<PokketProduct> getCachedProductList() {
        return cachedProductList;
    }

    /**
     * validate product in new order.
     * 1. product id should exist in latest product list.
     * 2. invest amount < remaining quota.
     *
     * @param productId
     * @param token
     * @param amount
     */
    public void validateProduct(Long productId, String token, BigDecimal amount) throws PokketServiceException {
        boolean productExist = false;
        for (PokketProduct p : cachedProductList) {
            if (p.getProductId().equals(productId)) {
                productExist = true;
                if (p.getToken().equalsIgnoreCase(token)) {
                    if (p.getRemainingQuota().compareTo(amount) < 0) {
                        throw new PokketServiceException(PokketUtil.ERROR_CODE_EXCEED_QUOTA);
                    }
                }
            }
        }
        if (!productExist) {
            throw new PokketServiceException(PokketUtil.ERROR_CODE_PRODUCT_EXPIRE);
        }
    }
}
