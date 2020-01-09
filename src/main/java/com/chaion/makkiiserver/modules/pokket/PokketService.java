package com.chaion.makkiiserver.modules.pokket;

import com.chaion.makkiiserver.blockchain.BlockchainException;
import com.chaion.makkiiserver.blockchain.TransactionConfirmEvent;
import com.chaion.makkiiserver.blockchain.TransactionStatus;
import com.chaion.makkiiserver.blockchain.btc.BtcService;
import com.chaion.makkiiserver.blockchain.eth.EthService;
import com.chaion.makkiiserver.modules.Notifier;
import com.chaion.makkiiserver.modules.pokket.model.PokketOrder;
import com.chaion.makkiiserver.modules.pokket.model.PokketOrderResult;
import com.chaion.makkiiserver.modules.pokket.model.PokketOrderStatus;
import com.chaion.makkiiserver.modules.pokket.model.PokketProduct;
import com.chaion.makkiiserver.modules.pokket.repository.PokketOrderRepository;
import com.chaion.makkiiserver.modules.pokket.security.AesProvider;
import com.chaion.makkiiserver.modules.pokket.security.CipherHelper;
import com.chaion.makkiiserver.modules.pokket.security.RsaProvider;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
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

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.chaion.makkiiserver.modules.pokket.PokketUtil.*;

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
    PokketOrderRepository repo;

    @Autowired
    EventBus eventBus;

    @Autowired
    RestTemplate restClient;

    @Autowired
    EthService ethService;

    @Autowired
    BtcService btcService;

    @Autowired
    Notifier notifier;

    @Value("${pokket.collateral_address}")
    String pokketCollateralAddress;

    @Value("${pokket.yield_collateral_address}")
    String pokketYieldCollateralAddress;

    private List<PokketProduct> cachedProductList = Collections.synchronizedList(new ArrayList<>());
    private Map<String, BigDecimal> cachedWithdrawFees = Collections.synchronizedMap(new HashMap<>());

    /**
     * pokket server base url
     */
    String baseUrl;

    private RsaProvider rsaProvider;

    @PostConstruct
    public void init() {
        logger.info("register PokketService to event bus.");
        eventBus.register(this);
    }

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
    private void createOrder(String orderId,
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
            product.setAllowedDecimals(productJo.get("allowed_decimals").getAsInt());
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
        logger.info("refresh pokket product list...");
        try {
            List<PokketProduct> products = getProducts();
            cachedProductList.clear();
            cachedProductList.addAll(products);
        } catch (Exception e) {
            logger.error("refresh product list exception: " + e.getMessage());
        }
    }

    public void refreshWithdrawFees() {
        logger.info("refersh pokket withdraw fee...");
        String url = baseUrl + "/products/withdrawfees";
        try {
            ResponseEntity<String> response = restClient.getForEntity(url, String.class);
            Map<String, BigDecimal> fees = new HashMap<>();
            JsonArray array = new JsonParser().parse(response.getBody()).getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                JsonObject item = array.get(i).getAsJsonObject();
                String token = item.get("token").getAsString();
                BigDecimal fee = item.get("fee").getAsBigDecimal();
                fees.put(token, fee);
            }
            cachedWithdrawFees.clear();
            cachedWithdrawFees.putAll(fees);
        } catch (HttpStatusCodeException e) {
            logger.error("refresh pokket withdraw fees exception: " + e.getMessage());
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

    @Subscribe
    public void handleTransactionConfirm(TransactionConfirmEvent event) {
        TransactionStatus status = event.getStatus();
        Map<String, Object> customData = status.getCustomData();
        if (customData != null && "Pokket".equalsIgnoreCase((String) customData.get(TransactionStatus.KEY_DOMAIN))) {
            if (PokketUtil.TX_FOR_INVESTMENT.equalsIgnoreCase((String) customData.get(PokketUtil.KEY_TX_FOR))) {
                logger.info("receive transaction confirm event for investment: " + status);
                String orderId = (String) customData.get(PokketUtil.KEY_ORDER_ID);
                Optional<PokketOrder> orderOpt = repo.findById(orderId);
                if (orderOpt.isPresent()) {
                    PokketOrder order = orderOpt.get();
                    if (status.getStatus().equalsIgnoreCase(TransactionStatus.CONFIRMED)) {
                        if (status.isResult()) {
                            logger.info("[pokket] invest transaction {} is confirmed as success", status.getTxId());
                            try {
                                createOrder(orderId,
                                        (Long) customData.get(PokketUtil.KEY_PRODUCT_ID),
                                        (String) customData.get(PokketUtil.KEY_INVESTMENT_ADDRESS),
                                        (String) customData.get(PokketUtil.KEY_COLLATERAL_ADDRESS),
                                        new BigDecimal((String) customData.get(PokketUtil.KEY_AMOUNT)),
                                        (Long) customData.get(PokketUtil.KEY_CURRENT_TIME),
                                        status.getTxId());
                                logger.info("[pokket] notify new order to pokket server");
                            } catch (Exception e) {
                                updateErrorStatus(order, "call pokket /deposit/deposit exception: " + e.getMessage());
                            }
                        } else {
                            updateErrorStatus(order,
                                    String.format("invest transaction %s is confirmed as failure", status.getTxId()));
                        }
                    } else if (status.getStatus().equalsIgnoreCase(TransactionStatus.TIMEOUT)) {
                        updateErrorStatus(order,
                                String.format("invest transaction %s time out", status.getTxId()));
                    }
                } else {
                    logger.error(orderId + " doesn't exist, skip event");
                }
            } else if (TX_FOR_COLLATERAL_SETTLEMENT.equalsIgnoreCase((String) customData.get(PokketUtil.KEY_TX_FOR))) {
                logger.info("receive transaction confirm event for collateral settlement: " + status);
                List<String> newOrderIds = (List<String>) customData.get(PokketUtil.KEY_NEW_ORDERS);
                BigDecimal amount = new BigDecimal((String) customData.get(PokketUtil.KEY_AMOUNT));
                boolean toDepositTusd = (Boolean) customData.get(PokketUtil.KEY_TO_DEPOSIT_TUSD);
                List<PokketOrder> newOrders = repo.findByOrderIdIn(newOrderIds);
                if (toDepositTusd) {
                    validateDepositCollateralTx(newOrders, status.getTxId(), amount);
                } else {
                    validateWithdrawCollateralTx(newOrders, status.getTxId(), amount);
                }
            } else if (PokketUtil.TX_FOR_COMPLETE.equalsIgnoreCase((String) customData.get(PokketUtil.KEY_TX_FOR))) {
                logger.info("receive transaction confirm event for complete: " + status);
                String orderId = (String) customData.get(PokketUtil.KEY_ORDER_ID);
                PokketOrderResult result = PokketOrderResult.valueOf((String) customData.get(KEY_RESULT));

                Optional<PokketOrder> orderOpt = repo.findById(orderId);
                if (orderOpt.isPresent()) {
                    PokketOrder order = orderOpt.get();
                    order.setResult(result);
                    if (result == PokketOrderResult.GREATER_THAN) {
                        validateYieldCollateralTxResultGreaterThan(status.getTxId(), order);
                    } else if (result == PokketOrderResult.LESS_THAN_NO_ROLL) {
                        String txHashReturnTUSD = (String) customData.get(KEY_TX_HASH_RETURN_TUSD);
                        validateYieldTokenTxResultLessThan(status.getTxId(), txHashReturnTUSD, order);
                    }
                } else {
                    logger.error("order " + orderId + " doesn't exist.");
                }
            }
        }
    }
    /**
     * Validate yield token transaction if result < 10%
     *
     * @param txHashYieldToken
     * @param order
     * @return
     */
    private void validateYieldTokenTxResultLessThan(String txHashYieldToken, String txHashReturnTUSD, PokketOrder order) {
        String token = order.getToken();
        String orderId = order.getOrderId();
        BigDecimal expectedAmount = PokketUtil.calculateProfit(order.getAmount(), order.getWeeklyInterestRate(), order.getAllowedDecimals());
        try {
            logger.info("======================== " + order.getOrderId() + " COMPLETE ============================");
            if (BTC.equalsIgnoreCase(token)) {
                BigInteger actualAmount = btcService.validateBtcTransaction(
                        txHashYieldToken,
                        POKKET_BTC_WALLET_ADDRESS,
                        order.getInvestorAddress(),
                        expectedAmount,
                        (a1, a2) -> true);
                order.setActualAmount(actualAmount);

                logger.info("expected amount: " + expectedAmount.toString());
                logger.info("actual amount: " + actualAmount.toString());
                logger.info("fee: " + cachedWithdrawFees.get("BTC"));
            } else if (ETH.equalsIgnoreCase(token)) {
                BigInteger actualAmount = ethService.validateEthTx(txHashYieldToken,
                        POKKET_ETH_WALLET_ADDRESS,
                        order.getInvestorAddress(),
                        expectedAmount,
                        // !!! WE DO NOT check actual amount because pokket dynamic reduce tokens that to cover eth transaction fee.
                        // !!! and eth->token rates are changing, pokket doesn't provide api to get this rate.
                        // (a1, a2)-> a1.compareTo(a2) == 0)) {
                        (a1, a2)-> true);
                order.setActualAmount(actualAmount);

                logger.info("expected amount: " + expectedAmount.scaleByPowerOfTen(18).toBigInteger().toString());
                logger.info("actual amount: " + actualAmount.toString());
                logger.info("fee: " + cachedWithdrawFees.get("ETH"));
            } else {
                BigInteger actualAmount = ethService.validateERC20Transaction(txHashYieldToken,
                        POKKET_ETH_WALLET_ADDRESS,
                        order.getInvestorAddress(),
                        order.getToken(),
                        expectedAmount,
                        // !!! WE DO NOT check actual amount because pokket dynamic reduce tokens that to cover eth transaction fee.
                        // !!! and eth->token rates are changing, pokket doesn't provide api to get this rate.
                        // (a1, a2) -> a1.compareTo(a2) == 0)) {
                        (a1, a2) -> true);
                order.setActualAmount(actualAmount);

                logger.info("expected amount: " + expectedAmount.scaleByPowerOfTen(18).toBigInteger().toString());
                logger.info("actual amount: " + actualAmount.toString());
                logger.info("fee: " + cachedWithdrawFees.get(order.getToken().toUpperCase()));
            }
            order.setYieldTokenTransactionHash(txHashYieldToken);
            order.setReturnTUSDTransactionHash(txHashReturnTUSD);
            order.setStatus(PokketOrderStatus.COMPLETE);
            repo.save(order);

            logger.info("=========================================================================================");

            notifyResult(order.getOrderId(), true, null);
        } catch (BlockchainException e) {
            updateErrorStatus(order, String.format("Finish order(%s) failed: yield token tx(%s) is invalid: %s",
                    orderId, txHashYieldToken, e.getMessage()));
        }
    }

    /**
     * Validate yield collateral transaction if result > 10%
     *
     * @param txHashYieldTUSD
     * @param order
     * @return
     */
    private void validateYieldCollateralTxResultGreaterThan(String txHashYieldTUSD, PokketOrder order) {
        BigDecimal expectedTUSD = PokketUtil.calculateCollateral(order.getAmount(),
                order.getToken2Collateral(),
                order.getWeeklyInterestRate());
        String expectedTo = order.getCollateralAddress() != null ? order.getCollateralAddress() : order.getInvestorAddress();
        try {
            BigInteger amount = ethService.validateERC20Transaction(txHashYieldTUSD,
                    pokketYieldCollateralAddress,
                    expectedTo,
                    TUSD,
                    expectedTUSD,
//                (a1, a2) -> a1.compareTo(a2) == 0)) {
                    (a1, a2) -> true);
            order.setActualAmount(amount);
            order.setYieldTUSDTransactionHash(txHashYieldTUSD);
            order.setStatus(PokketOrderStatus.COMPLETE);
            repo.save(order);

            logger.info("======================== " + order.getOrderId() + " COMPLETE ============================");
            logger.info("expected amount: " + expectedTUSD.scaleByPowerOfTen(18).toBigInteger().toString());
            logger.info("actual amount: " + amount.toString());
            logger.info("fee: " + cachedWithdrawFees.get(TUSD));
            logger.info("=========================================================================================");

            notifyResult(order.getOrderId(), true, null);
        } catch (BlockchainException e) {
            logger.error(e.getMessage());
            updateErrorStatus(order, String.format("Finish order(%s) failed: Yield TUSD transaction(%s) is invalid",
                    order.getOrderId(), txHashYieldTUSD));
        }
    }

    /**
     * Validate collateral withdraw transaction.
     *
     * @param newOrders
     * @param transactionHash
     * @param tusdTransfer
     */
    private void validateWithdrawCollateralTx(List<PokketOrder> newOrders,
                                              String transactionHash,
                                              BigDecimal tusdTransfer) {
        try {
            BigInteger amount = ethService.validateERC20Transaction(transactionHash,
                    pokketCollateralAddress,
                    POKKET_ETH_WALLET_ADDRESS,
                    TUSD,
                    tusdTransfer.abs(),
                    (a1, a2) -> a1.compareTo(a2) >= 0);
            logger.info("withdraw tusd tx:" + transactionHash + ", actual amount:" + amount.toString());
            for (PokketOrder order : newOrders) {
                order.setStatus(PokketOrderStatus.IN_PROGRESS);
                order.setStartTime(System.currentTimeMillis());
                repo.save(order);
            }
        } catch (BlockchainException e) {
            logger.error(e.getMessage());
            String errMsg = String.format("Invalid tusd withdraw txhash %s, expected withdraw amount is %s",
                    transactionHash, tusdTransfer);
            for (PokketOrder order : newOrders) {
                updateErrorStatus(order, errMsg);
            }
        }
    }

    /**
     * Validate collateral deposit transaction.
     *
     * @param newOrders
     * @param transactionHash
     * @param tusdTransfer
     */
    private void validateDepositCollateralTx(List<PokketOrder> newOrders,
                                             String transactionHash,
                                             BigDecimal tusdTransfer) {
        try {
            BigInteger amount = ethService.validateERC20Transaction(transactionHash,
                    POKKET_ETH_WALLET_ADDRESS,
                    pokketCollateralAddress,
                    TUSD,
                    tusdTransfer.abs(),
                    (a1, a2) -> a1.compareTo(a2) >= 0);
            logger.info("deposit tusd tx:" + transactionHash + ", actual amount:" + amount.toString());

            for (PokketOrder order : newOrders) {
                order.setStatus(PokketOrderStatus.IN_PROGRESS);
                order.setStartTime(System.currentTimeMillis());
                repo.save(order);
            }
        } catch(BlockchainException e) {
            logger.error(e.getMessage());
            String errMsg = String.format("Invalid tusd deposit txhash %s , expected deposit amount is %s",
                    transactionHash, tusdTransfer);
            for (PokketOrder order : newOrders) {
                updateErrorStatus(order, errMsg);
            }
        }
    }

    protected void updateErrorStatus(PokketOrder order, String message) {
        order.addErrorItem(message);
        logger.error(message);
        repo.save(order);

        notifyResult(order.getOrderId(), false, message);
    }

    private void notifyResult(String orderId, boolean success, String errorMessage) {
        JsonObject msg = new JsonObject();
        msg.addProperty("text", "");
        JsonArray attachments = new JsonArray();
        JsonObject attach = new JsonObject();
        attach.addProperty("color", success? "#66CD00": "#FF0000");
        attach.addProperty("text", "Pokket Order " + orderId + (success?"": ": " + errorMessage));
        attachments.add(attach);
        msg.add("attachments", attachments);
        notifier.notify(msg.toString());
    }
}
