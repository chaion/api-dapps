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

import static com.chaion.makkiiserver.modules.pokket.PokketUtil.*;

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
            watchInvestTx(req, orderId, currentTime, txId, blockchain);
            logger.info("[pokket] add tx {} to pending queue, wait confirmation.", txId);
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
        // not implement this feature in this phase
//        order.setAutoRoll(req.isAutoRoll());
        order.setProductId(req.getProductId());
        order.setCreateTime(currentTime);
        order.setStatus(PokketOrderStatus.WAIT_INVEST_TX_CONFIRM);
        repo.save(order);

        return order;
    }

    private void watchInvestTx(CreateOrderReq req, String orderId, long currentTime,
                               String txId, BaseBlockchain blockchain) {
        blockchain.addPendingTransaction(txId, (transactionHash, status) -> {
            PokketOrder order = getOrder(orderId);
            if (status) {
                logger.info("[pokket] invest transaction {} is confirmed as success", txId);
                Long pokketOrderId = null;
                try {
                    pokketOrderId = pokketService.createOrder(orderId,
                            req.getProductId(),
                            req.getInvestorAddress(),
                            req.getCollateralAddress(),
                            req.getAmount(),
                            currentTime,
                            transactionHash);
                    order.setPokketOrderId(pokketOrderId);
                    logger.info("[pokket] notify new order to pokket server: pokket order id=" + pokketOrderId);
                    order.setStatus(PokketOrderStatus.WAIT_COLLATERAL_DEPOSIT);
                    logger.info("[pokket] update status to WAIT_COLLATERAL_DEPOSIT");
                    repo.save(order);
                } catch (Exception e) {
                    updateErrorStatus(order, "call pokket /deposit/deposit exception: " + e.getMessage());
                }
            } else {
                updateErrorStatus(order, String.format("invest transaction %s is confirmed as failure" ,txId));
            }
        });
    }

    @ApiOperation(value="每日备用金净值清算后调用")
    @PostMapping("/order/collateralSettlement")
    public void collateralSettlement(@RequestBody CollateralSettlementReq collateralSettlementReq) {
        List<PokketOrder> closedOrders = repo.findByOrderIdIn(collateralSettlementReq.getClosedOrderIds());
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
                    updateErrorStatus(newOrder, errMsg);
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errMsg);
            }

            if (signum > 0) {
                validateDepositCollateralTx(newOrders, transactionHash, tusdTransfer);
            } else {
                validateWithdrawCollateralTx(newOrders, transactionHash, tusdTransfer);
            }
        } else {
            logger.info("tusd withdraw and deposit are equal, no transaction required.");
        }

        for (PokketOrder newOrder : newOrders) {
            newOrder.setStatus(PokketOrderStatus.IN_PROGRESS);
            newOrder.setStartTime(System.currentTimeMillis());
            repo.save(newOrder);
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
        if (!ethService.validateERC20Transaction(transactionHash,
                MAKKII_WALLET_ADDRESS,
                POKKET_ETH_WALLET_ADDRESS,
                TUSD,
                tusdTransfer.abs(),
                (a1, a2) -> a1.compareTo(a2) <= 0)) {
            String errMsg = String.format("Invalid tusd withdraw txhash %s, expected withdraw amount is %s",
                    transactionHash, tusdTransfer);
            for (PokketOrder order : newOrders) {
                updateErrorStatus(order, errMsg);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errMsg);
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
        if (!ethService.validateERC20Transaction(transactionHash,
                POKKET_ETH_WALLET_ADDRESS,
                MAKKII_WALLET_ADDRESS,
                TUSD,
                tusdTransfer.abs(),
                (a1, a2) -> a1.compareTo(a2) >= 0)) {
            String errMsg = String.format("Invalid tusd deposit txhash %s , expected deposit amount is %s",
                        transactionHash, tusdTransfer);
            for (PokketOrder order : newOrders) {
                updateErrorStatus(order, errMsg);
            }

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errMsg);
        }
    }

    @ApiOperation(value="每日完成订单通知")
    @PostMapping("/order/complete")
    public List<String> finishOrders(@RequestBody List<FinishOrderReq> finishOrderReqList) {
        List<String> errorOrderIds = new ArrayList<>();
        for (FinishOrderReq req : finishOrderReqList) {
            PokketOrderResult result = req.getResult();
            String orderId = req.getOrderId();
            boolean updateResult = true;
            if (result == PokketOrderResult.LESS_THAN_NO_ROLL) {
                updateResult = handleResultLessThanNoRoll(orderId, req.getTxHashYieldToken(), req.getTxHashReturnTUSD());
            } else if (result == PokketOrderResult.LESS_THAN_ROLL) {
                // TODO: current not support rolling feature
                throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
            } else if (result == PokketOrderResult.GREATER_THAN) {
                updateResult = handleResultGreaterThan(orderId, req.getTxHashYieldTUSD());
            }
            if (!updateResult) {
                errorOrderIds.add(orderId);
            }
        }
        return errorOrderIds;
    }

    /**
     * Finish order process if result > 10%.
     *
     * @param orderId
     * @param txHashYieldTUSD
     * @return
     */
    private boolean handleResultGreaterThan(String orderId, String txHashYieldTUSD) {
        PokketOrder order = getOrder(orderId);
        order.setResult(PokketOrderResult.GREATER_THAN);
        if (txHashYieldTUSD == null) {
            updateErrorStatus(order, String.format("Finish order(%s) failed: txHashYieldTUSD=%s", txHashYieldTUSD));
            return false;
        }

        if (validateYieldCollateralTxResultGreaterThan(txHashYieldTUSD, order)) return false;

        order.setYieldTUSDTransactionHash(txHashYieldTUSD);
        order.setStatus(PokketOrderStatus.COMPLETE);
        repo.save(order);
        return true;
    }

    /**
     * Validate yield collateral transaction if result > 10%
     *
     * @param txHashYieldTUSD
     * @param order
     * @return
     */
    private boolean validateYieldCollateralTxResultGreaterThan(String txHashYieldTUSD, PokketOrder order) {
        BigDecimal expectedTUSD = PokketUtil.calculateCollateral(order.getAmount(),
                order.getToken2Collateral(),
                order.getWeeklyInterestRate());
        String expectedTo = order.getCollateralAddress() != null ? order.getCollateralAddress() : order.getInvestorAddress();
        // TODO: validate from/to
        if (!ethService.validateERC20Transaction(txHashYieldTUSD,
                POKKET_ETH_WALLET_ADDRESS,
                null/*expectedTo*/,
                TUSD,
                expectedTUSD,
                (a1, a2) -> a1.compareTo(a2) == 0)) {
            updateErrorStatus(order, String.format("Finish order(%s) failed: Yield TUSD transaction(%s) is invalid",
                    order.getOrderId(), txHashYieldTUSD));
            return true;
        }
        return false;
    }

    /**
     * Finish order process if result < 10%
     *
     * @param orderId
     * @param txHashYieldToken
     * @param txHashReturnTUSD
     * @return
     */
    private boolean handleResultLessThanNoRoll(String orderId, String txHashYieldToken, String txHashReturnTUSD) {
        PokketOrder order = getOrder(orderId);
        order.setResult(PokketOrderResult.LESS_THAN_NO_ROLL);
        if (txHashYieldToken == null || txHashReturnTUSD == null) {
            updateErrorStatus(order, String.format("Finish order(%s) failed: " +
                    "txHashYieldToken(%s) or txHashReturnTUSD(%s) is missing. ",
                    order.getOrderId(), txHashYieldToken, txHashReturnTUSD));
            return false;
        }

        if (!validateYieldTokenTxResultLessThan(txHashYieldToken, order)) return false;
        // no need to validate return tusd transaction hash since it's not for this order but for all orders including
        // new invest and closed orders.
//        if (!validateReturnCollateralTxResultLessThan(txHashReturnTUSD, order)) return false;

        order.setYieldTokenTransactionHash(txHashYieldToken);
        order.setReturnTUSDTransactionHash(txHashReturnTUSD);
        order.setStatus(PokketOrderStatus.COMPLETE);
        repo.save(order);
        return true;
    }

    /**
     * Validate return tusd transaction if result < 10%
     *
     * @param txHashReturnTUSD
     * @param order
     * @return
     */
    private boolean validateReturnCollateralTxResultLessThan(String txHashReturnTUSD, PokketOrder order) {
        BigDecimal expectedTUSD = PokketUtil.calculateCollateral(order.getAmount(),
                order.getToken2Collateral(),
                order.getWeeklyInterestRate());
        if (!ethService.validateERC20Transaction(txHashReturnTUSD,
                MAKKII_WALLET_ADDRESS,
                POKKET_ETH_WALLET_ADDRESS,
                TUSD,
                expectedTUSD,
                (a1, a2) -> a1.compareTo(a2) == 0)) {
            updateErrorStatus(order, String.format("Finish order(%s) failed: Return TUSD transaction(%s) is invalid", order.getOrderId(), txHashReturnTUSD));
            return false;
        }
        return true;
    }

    /**
     * Validate yield token transaction if result < 10%
     *
     * @param txHashYieldToken
     * @param order
     * @return
     */
    private boolean validateYieldTokenTxResultLessThan(String txHashYieldToken, PokketOrder order) {
        String token = order.getToken();
        String orderId = order.getOrderId();
        BigDecimal expectedAmount = PokketUtil.calculateProfit(order.getAmount(), order.getWeeklyInterestRate());
        if (BTC.equalsIgnoreCase(token)) {
            try {
                btcService.validateBtcTransaction(
                        txHashYieldToken,
                        POKKET_BTC_WALLET_ADDRESS,
                        null/*order.getInvestorAddress()*/,
                        expectedAmount);
            } catch (BlockchainException e) {
                updateErrorStatus(order, String.format("Finish order(%s) failed: yield token tx(%s) is invalid: %s",
                        order, txHashYieldToken, e.getMessage()));
                return false;
            }
        } else if (ETH.equalsIgnoreCase(token)) {
            if (!ethService.validateEthTx(txHashYieldToken,
                    POKKET_ETH_WALLET_ADDRESS,
                    null/*order.getInvestorAddress()*/,
                    expectedAmount)) {
                updateErrorStatus(order,
                        String.format("Finish order(%s) failed: Yield token transaction(%s) is invalid",
                                orderId, txHashYieldToken));
                return false;
            }
        } else {
            if (!ethService.validateERC20Transaction(txHashYieldToken,
                    POKKET_ETH_WALLET_ADDRESS,
                    null/*order.getInvestorAddress()*/,
                    order.getToken(),
                    expectedAmount,
                    (a1, a2) -> a1.compareTo(a2) == 0)) {
                updateErrorStatus(order, String.format("Finish Order(%s) failed: Yield token transaction(%s) is invalid",
                        orderId, txHashYieldToken));
                return false;
            }
        }
        return true;
    }

    @ApiOperation(value="Get investors' orders")
    @GetMapping("/order")
    public Page<PokketOrder> getOrder(@RequestBody GetOrderReq getOrderReq) {
        List<String> addressList = getOrderReq.getAddresses();

        Sort sort = new Sort(Sort.Direction.DESC, "status", "createTime");
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
        PokketOrder order = getOrder(orderId);
        List<ErrorItem> errors = order.getErrors();
        for (ErrorItem item : errors) {
            item.setResolved(true);
        }
        repo.save(order);
    }

    /**
     * Fetch pokket order from db.
     *
     * @param orderId
     * @return
     */
    private PokketOrder getOrder(String orderId) {
        Optional<PokketOrder> orderOpt = repo.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order(" + orderId + ") is not found.");
        }
        return orderOpt.get();
    }

    private void updateErrorStatus(PokketOrder order, String message) {
        order.addErrorItem(message);
        logger.error(message);
        repo.save(order);
    }

}
