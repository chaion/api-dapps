package com.chaion.makkiiserver.modules.pokket;

import com.chaion.makkiiserver.blockchain.BaseBlockchain;
import com.chaion.makkiiserver.blockchain.btc.BtcService;
import com.chaion.makkiiserver.blockchain.BlockchainException;
import com.chaion.makkiiserver.modules.config.ModuleConfig;
import com.chaion.makkiiserver.modules.config.ModuleConfigRepository;
import com.chaion.makkiiserver.modules.pokket.model.*;
import com.chaion.makkiiserver.modules.pokket.repository.PokketOrderRepository;
import com.chaion.makkiiserver.blockchain.eth.EthService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

import static com.chaion.makkiiserver.blockchain.TransactionStatus.KEY_DOMAIN;
import static com.chaion.makkiiserver.modules.pokket.PokketUtil.*;

/**
 * No preauthorized is set, we won't restrict pokket access for now.
 */
@Api(value = "Pokket Financial APIs", description="Some Apis are for wallet, some are for pokket server")
@RestController
@RequestMapping("pokket")
public class PokketController {

    private static final Logger logger = LoggerFactory.getLogger(PokketController.class);

    @Autowired
    PokketOrderRepository repo;
    @Autowired
    PokketService pokketService;
    @Autowired
    EthService ethService;
    @Autowired
    BtcService btcService;
    @Autowired
    ModuleConfigRepository moduleRepo;

    @PutMapping("/order")
    public PokketOrder createOrder(@RequestBody CreateOrderReq req) {
        try {
            pokketService.validateProduct(req.getProductId(), req.getToken(), req.getAmount());
        } catch (PokketServiceException e) {
            logger.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        logger.info("[pokket] validate new order(productId={},token={},amount={}) passed.",
                req.getProductId(), req.getToken(), req.getAmount());

        final String orderId = PokketUtil.generateOrderId();
        logger.info("[pokket] receive new order request, generate order id=" + orderId);
        long currentTime = System.currentTimeMillis();

        String rawTransaction = req.getRawTransaction();
        String txId = null;
        BaseBlockchain blockchain = null;
        try {
            if (req.getToken().equalsIgnoreCase(BTC)) {
                blockchain = btcService;
            } else {
                blockchain = ethService;
            }
            txId = blockchain.sendRawTransaction(rawTransaction);
            logger.info("[pokket][{}] sending invest raw transaction {}", req.getToken(), txId);

            Map<String, Object> customData = new HashMap<>();
            customData.put(KEY_DOMAIN, "Pokket");
            customData.put(KEY_TX_FOR, TX_FOR_INVESTMENT);
            customData.put(KEY_ORDER_ID, orderId);
            customData.put(KEY_PRODUCT_ID, req.getProductId());
            customData.put(KEY_INVESTMENT_ADDRESS, req.getInvestorAddress());
            customData.put(KEY_COLLATERAL_ADDRESS, req.getCollateralAddress());
            customData.put(KEY_AMOUNT, req.getAmount());
            customData.put(KEY_CURRENT_TIME, currentTime);
            blockchain.addPendingTransaction(txId, customData);
            logger.info("[pokket] add invest tx {} to pending queue, wait confirmation.", txId);
        } catch (BlockchainException e) {
            logger.error("[pokket] send invest raw transaction exception: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid rawTransaction");
        }

        PokketOrder order = new PokketOrder();
        order.setOrderId(orderId);
        order.setInvestorAddress(req.getInvestorAddress());
        String collateralAddress = req.getCollateralAddress();
        if (collateralAddress != null) {
            order.setCollateralAddress(collateralAddress);
        }
        order.setToken(req.getToken());
        order.setTokenFullName(req.getTokenFullName());
        order.setAmount(req.getAmount());
        order.setWeeklyInterestRate(req.getWeeklyInterestRate());
        order.setYearlyInterestRate(req.getYearlyInterestRate());
        order.setInvestTransactionHash(txId);
        order.setToken2Collateral(req.getToken2Collateral());
        order.setAllowedDecimals(req.getAllowedDecimals());
        // not implement this feature in this phase
//        order.setAutoRoll(req.isAutoRoll());
        order.setProductId(req.getProductId());
        order.setCreateTime(currentTime);
        order.setStatus(PokketOrderStatus.WAIT_INVEST_TX_CONFIRM);
        repo.save(order);

        return order;
    }

    @PostMapping("/order/depositConfirm")
    public void depositConfirm(@RequestBody OrderIdReq req) {
        Optional<PokketOrder> orderOpt = repo.findById(req.getExternalOrderId());
        if (!orderOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "order " + req.getExternalOrderId() + " not found.");
        }
        PokketOrder order = orderOpt.get();
        if (req.getSuccess()) {
            order.setPokketOrderId(req.getOrderId());
            order.setStatus(PokketOrderStatus.WAIT_COLLATERAL_DEPOSIT);
            logger.info("[pokket] update status to WAIT_COLLATERAL_DEPOSIT");
            repo.save(order);
        } else {
            pokketService.updateErrorStatus(order, req.getErrorMessage());
        }
    }

    @ApiOperation(value="每日备用金净值清算后调用")
    @PostMapping("/order/collateralSettlement")
    public void collateralSettlement(@RequestBody CollateralSettlementReq collateralSettlementReq) {
        List<PokketOrder> closedOrders = repo.findByOrderIdIn(collateralSettlementReq.getClosedOrderIdsReturnTUSD());
        BigDecimal totalWithdrawTUSD = calculateTotalWithdrawCollateral(closedOrders);

        List<PokketOrder> newOrders = repo.findByOrderIdIn(collateralSettlementReq.getNewOrderIds());
        BigDecimal totalDepositTUSD = calculateTotalDepositCollateral(collateralSettlementReq, newOrders);

        String transactionHash = collateralSettlementReq.getTransactionHash();
        BigDecimal tusdTransfer = totalDepositTUSD.subtract(totalWithdrawTUSD);
        int signum = tusdTransfer.signum();
        if (signum != 0) {
            if (transactionHash == null) {
                String errMsg = String.format("collateral settlement fail: " +
                            "%s tusd should be transfered, but txhash is missing.", tusdTransfer);
                for (PokketOrder newOrder : newOrders) {
                    pokketService.updateErrorStatus(newOrder, errMsg);
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errMsg);
            }

            Map<String, Object> customData = new HashMap<>();
            customData.put(KEY_DOMAIN, "Pokket");
            customData.put(KEY_TX_FOR, TX_FOR_COLLATERAL_SETTLEMENT);
            customData.put(KEY_NEW_ORDERS, collateralSettlementReq.getNewOrderIds());
            customData.put(KEY_AMOUNT, tusdTransfer);
            customData.put(KEY_TO_DEPOSIT_TUSD, signum > 0);
            customData.put(KEY_CURRENT_TIME, System.currentTimeMillis());
            logger.info("[pokket] add collateral settlement tx {} to pending queue, wait confirmation.", transactionHash);
            ethService.addPendingTransaction(transactionHash, customData);
        } else {
            logger.info("tusd withdraw and deposit are equal, no transaction required.");

            for (PokketOrder newOrder : newOrders) {
                newOrder.setStatus(PokketOrderStatus.IN_PROGRESS);
                newOrder.setStartTime(System.currentTimeMillis());
                repo.save(newOrder);
            }
        }
    }

    /**
     * calculate total collateral that should be deposit according to closed orders.
     *
     * @param collateralSettlementReq
     * @param newOrders
     * @return
     */
    private BigDecimal calculateTotalDepositCollateral(@RequestBody CollateralSettlementReq collateralSettlementReq, List<PokketOrder> newOrders) {
        logger.info("calculating should total deposit collateral...");
        Map<String, BigDecimal> ratios = collateralSettlementReq.getToken2CollateralMap();
        BigDecimal totalDepositTUSD = new BigDecimal("0");
        for (PokketOrder order : newOrders) {
            order.setDepositTUSDTransactionHash(collateralSettlementReq.getTransactionHash());
            order.setToken2Collateral(ratios.get(order.getToken()));

            BigDecimal amount = order.getAmount();
            BigDecimal token2Collateral = order.getToken2Collateral();
            BigDecimal weeklyInterest = order.getWeeklyInterestRate();
            BigDecimal tusd = calculateCollateral(amount, token2Collateral, weeklyInterest);
            totalDepositTUSD = totalDepositTUSD.add(tusd);
            logger.info(order.getOrderId() + "(" + order.getPokketOrderId() + "):amount=" + amount + ",tusd=" + tusd);
            logger.info("newOrder({},pokket order id={}) amount={},tusd={}",
                    order.getOrderId(), order.getPokketOrderId(), amount, tusd);
        }
        logger.info("should total deposit collateral is " + totalDepositTUSD.toString());
        return totalDepositTUSD;
    }

    /**
     * calculate total collateral that should be withdrawed according to new orders.
     *
     * @param closedOrders
     * @return
     */
    private BigDecimal calculateTotalWithdrawCollateral(List<PokketOrder> closedOrders) {
        logger.info("calculating should total withdraw collateral...");
        BigDecimal totalWithdrawTUSD = new BigDecimal("0");
        // calculate collateral net
        for (PokketOrder order : closedOrders) {
            BigDecimal amount = order.getAmount();
            BigDecimal token2Collateral = order.getToken2Collateral();
            BigDecimal weeklyInterest = order.getWeeklyInterestRate();
            BigDecimal tusd = calculateCollateral(amount, token2Collateral, weeklyInterest);
            totalWithdrawTUSD = totalWithdrawTUSD.add(tusd);
            logger.info("closeOrder({},pokket order id={}) amount={},tusd={}",
                    order.getOrderId(), order.getPokketOrderId(), amount, tusd);
        }
        logger.info("should total withdraw tusd is " + totalWithdrawTUSD.toString());
        return totalWithdrawTUSD;
    }

    @ApiOperation(value="每日完成订单通知")
    @PostMapping("/order/complete")
    public void finishOrders(@RequestBody List<FinishOrderReq> finishOrderReqList) {
        for (FinishOrderReq req : finishOrderReqList) {
            PokketOrderResult result = req.getResult();
            String orderId = req.getOrderId();
            Optional<PokketOrder> orderOpt = repo.findById(orderId);
            if (orderOpt.isPresent()) {
                PokketOrder order = orderOpt.get();
                order.setResult(result);
                if (result == PokketOrderResult.LESS_THAN_NO_ROLL) {
                    if (req.getTxHashYieldToken() == null || req.getTxHashReturnTUSD() == null) {
                        pokketService.updateErrorStatus(order, String.format("Finish order(%s) failed: " +
                                        "txHashYieldToken(%s) or txHashReturnTUSD(%s) is missing. ",
                                order.getOrderId(), req.getTxHashYieldToken(), req.getTxHashReturnTUSD()));
                    } else {
                        Map<String, Object> customData = new HashMap<>();
                        customData.put(KEY_DOMAIN, "Pokket");
                        customData.put(KEY_TX_FOR, TX_FOR_COMPLETE);
                        customData.put(KEY_ORDER_ID, orderId);
                        customData.put(KEY_RESULT, result);
                        customData.put(KEY_TX_HASH_RETURN_TUSD, req.getTxHashReturnTUSD());
                        ethService.addPendingTransaction(req.getTxHashYieldToken(), customData);
                    }
                } else if (result == PokketOrderResult.LESS_THAN_ROLL) {
                    // TODO: current not support rolling feature
                    throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
                } else if (result == PokketOrderResult.GREATER_THAN) {
                    if (req.getTxHashYieldTUSD() == null) {
                        pokketService.updateErrorStatus(order,
                                String.format("Finish order(%s) failed: txHashYieldTUSD=%s",
                                        req.getTxHashYieldTUSD()));
                    } else {
                        Map<String, Object> customData = new HashMap<>();
                        customData.put(KEY_DOMAIN, "Pokket");
                        customData.put(KEY_TX_FOR, TX_FOR_COMPLETE);
                        customData.put(KEY_ORDER_ID, orderId);
                        customData.put(KEY_RESULT, result);
                        ethService.addPendingTransaction(req.getTxHashYieldTUSD(), customData);
                    }
                }
            } else {
                logger.error("order " + orderId + " doesn't exist.");
            }
        }
    }


    @ApiOperation(value="Get investors' orders")
    @PostMapping("/order")
    public Page<PokketOrder> getOrder(@RequestBody GetOrderReq getOrderReq) {
        List<String> addressList = getOrderReq.getAddresses();

        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Page<PokketOrder> orders = repo.findByInvestorAddressIn(addressList,
                PageRequest.of(getOrderReq.getPage(), getOrderReq.getSize(), sort));
        return orders;
    }

    @PostMapping("/getorders")
    public Page<PokketOrder> getOrders(@RequestBody QueryOrderReq req) {
        return repo.findAdvanced(req);
    }

    @ApiOperation(value="Get all financial products")
    @GetMapping("/product")
    public List<PokketProduct> getProducts(@RequestParam(value="search", required = false) String search) {
//        if (search == null || search.isEmpty()) {
        return pokketService.getCachedProductList();
//        } else {
//        }
    }

    @ApiOperation(value="Get pokket's total deposit amount, including deposits on pokket's website")
    @GetMapping("/statistic/totalInvestment")
    public BigDecimal getTotalInvestment() {
        try {
            return pokketService.getTotalInvestment();
        } catch (PokketServiceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @ApiOperation(value="Get pokket's Bitcoin address which investors should transfer ETH/ERC20 to")
    @GetMapping("/deposit/btc_address")
    public String getDepositBtcAddress() {
        try {
            return pokketService.getDepositAddress(PokketService.ADDRESS_TYPE_BITCOIN);
        } catch (PokketServiceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @ApiOperation(value="Get pokket's ethereum address which investors should transfer ETH/ERC20 to")
    @GetMapping("/deposit/eth_address")
    public String getDepositEthAddress() {
        try {
            return pokketService.getDepositAddress(PokketService.ADDRESS_TYPE_ETH);
        } catch (PokketServiceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @ApiOperation(value="Get pokket financial banner images for advertisement use.",
        notes = "response is an array containing image download url")
    @GetMapping("/banners")
    public List<Banner> getBanners() {
        List<Banner> banners = new ArrayList<>();
        ModuleConfig config = moduleRepo.findFirstByModuleNameIgnoreCase("Pokket");
        Map<String, String> params = config.getModuleParams();
        if (params != null && params.containsKey("banners")) {
            JsonArray bannerArray = new JsonParser().parse(params.get("banners")).getAsJsonArray();
            for (int i = 0; i < bannerArray.size(); i++) {
                JsonObject joBanner = bannerArray.get(i).getAsJsonObject();
                Banner banner = new Banner();
                banner.setImageUrl(joBanner.get("banner_url").getAsString());
                if (joBanner.has("banner_link")) {
                    banner.setLink(joBanner.get("banner_link").getAsString());
                }
                banners.add(banner);
            }
        }

        return banners;
    }

    @PostMapping("/resolveOrder/{orderId}")
    public void resolveError(@PathVariable("orderId") String orderId) {
        Optional<PokketOrder> orderOpt = repo.findById(orderId);
        if (orderOpt.isPresent()) {
            PokketOrder order = orderOpt.get();
            List<ErrorItem> errors = order.getErrors();
            for (ErrorItem item : errors) {
                item.setResolved(true);
            }
            repo.save(order);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "order " + orderId + " doesn't exist.");
        }
    }

}
