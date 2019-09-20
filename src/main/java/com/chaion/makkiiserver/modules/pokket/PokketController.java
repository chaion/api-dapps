package com.chaion.makkiiserver.modules.pokket;

import com.chaion.makkiiserver.blockchain.BaseBlockchain;
import com.chaion.makkiiserver.blockchain.btc.BtcService;
import com.chaion.makkiiserver.blockchain.eth.BlockchainException;
import com.chaion.makkiiserver.modules.pokket.model.*;
import com.chaion.makkiiserver.modules.pokket.repository.PokketOrderRepository;
import com.chaion.makkiiserver.blockchain.eth.EthService;
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

    @Autowired PokketOrderRepository repo;
    @Autowired PokketService pokketService;
    @Autowired EthService ethService;
    @Autowired
    BtcService btcService;

    @PutMapping("/order")
    public PokketOrder createOrder(@RequestBody CreateOrderReq req) {
        // validate product
        pokketService.validateProduct(req.getProductId(), req.getToken(), req.getAmount());

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
            logger.info("[pokket][" + req.getToken() + "] sending invest raw transaction: txId=" + txId);
            blockchain.addPendingTransaction(txId, (transactionHash, status) -> {
                PokketOrder order = getOrder(orderId);
                if (status) {
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

                        logger.info("[pokket] invest transaction is confirmed, update status to WAIT_COLLATERAL_DEPOSIT");
                        order.setStatus(PokketOrderStatus.WAIT_COLLATERAL_DEPOSIT);
                    } catch (Exception e) {
                        logger.error("call pokket createOrder exception: " + e.getMessage(), e);
                        order.setStatus(PokketOrderStatus.ERROR);
                        order.setErrorMessage("call pokket /deposit/deposit exception" + e.getMessage());
                    }
                } else {
                    logger.error("[pokket][" + orderId + "] invest transaction confirmed as failed");
                    order.setErrorMessage("invest transaction failed");
                    order.setStatus(PokketOrderStatus.ERROR);
                }
                repo.save(order);
            });
        } catch (BlockchainException e) {
            logger.error("[pokket]sendRawTransaction exception: " + e.getMessage());
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

    @ApiOperation(value="每日备用金净值清算后调用")
    @PostMapping("/order/collateralSettlement")
    public void collateralSettlement(@RequestBody CollateralSettlementReq collateralSettlementReq) {
        List<PokketOrder> closedOrders = repo.findByOrderIdIn(collateralSettlementReq.getClosedOrderIds());
        BigDecimal totalWithdrawTUSD = calculateTotalReturnCollateral(closedOrders);

        List<PokketOrder> newOrders = repo.findByOrderIdIn(collateralSettlementReq.getNewOrderIds());
        BigDecimal totalDepositTUSD = calculateTotalDepositCollateral(collateralSettlementReq, newOrders);

        String transactionHash = collateralSettlementReq.getTransactionHash();
        BigDecimal tusdTransfer = totalDepositTUSD.subtract(totalWithdrawTUSD);
        if (tusdTransfer.signum() != 0) {
            if (transactionHash == null) {
                for (PokketOrder newOrder : newOrders) {
                    newOrder.setStatus(PokketOrderStatus.ERROR);
                    newOrder.setErrorMessage("collateral settlement request: " + collateralSettlementReq.toString() + ". "
                            + tusdTransfer + " should transfer, but transaction hash is missing.");
                    repo.save(newOrder);
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, tusdTransfer + " tusd should be transfered but transactionHash is missing.");
            }
        }

        if (tusdTransfer.signum() > 0) {
            validateShouldDepositCollateral(newOrders, totalWithdrawTUSD, totalDepositTUSD, transactionHash, tusdTransfer);
        } else if (tusdTransfer.signum() < 0) {
            validateShouldReturnCollateral(newOrders, totalWithdrawTUSD, totalDepositTUSD, transactionHash, tusdTransfer);
        } else {
            logger.info("tusd withdraw and deposit are equal, no transaction required.");
        }

        for (PokketOrder newOrder : newOrders) {
            newOrder.setStatus(PokketOrderStatus.IN_PROGRESS);
            newOrder.setStartTime(System.currentTimeMillis());
            repo.save(newOrder);
        }
    }

    private BigDecimal calculateTotalDepositCollateral(@RequestBody CollateralSettlementReq collateralSettlementReq, List<PokketOrder> newOrders) {
        logger.info("---- new orders ----");
        Map<String, BigDecimal> ratios = collateralSettlementReq.getToken2CollateralMap();
        BigDecimal totalDepositTUSD = new BigDecimal("0");
        for (PokketOrder newOrder : newOrders) {
            newOrder.setDepositTUSDTransactionHash(collateralSettlementReq.getTransactionHash());
            newOrder.setToken2Collateral(ratios.get(newOrder.getToken()));
            BigDecimal amount = newOrder.getAmount();
            BigDecimal token2Collateral = newOrder.getToken2Collateral();
            BigDecimal weeklyInterest = newOrder.getWeeklyInterestRate();
            BigDecimal tusd = calculateCollateral(amount, token2Collateral, weeklyInterest);
            totalDepositTUSD = totalDepositTUSD.add(tusd);
            logger.info(newOrder.getOrderId() + "(" + newOrder.getPokketOrderId() + "):amount=" + amount + ",tusd=" + tusd);
        }
        logger.info("total deposit tusd=" + totalDepositTUSD.toString());
        return totalDepositTUSD;
    }

    private BigDecimal calculateTotalReturnCollateral(List<PokketOrder> closedOrders) {
        BigDecimal totalWithdrawTUSD = new BigDecimal("0");
        logger.info("---- closed orders ----");
        // calculate collateral net
        for (PokketOrder closedOrder : closedOrders) {
            BigDecimal amount = closedOrder.getAmount();
            BigDecimal token2Collateral = closedOrder.getToken2Collateral();
            BigDecimal weeklyInterest = closedOrder.getWeeklyInterestRate();
            BigDecimal tusd = calculateCollateral(amount, token2Collateral, weeklyInterest);
            totalWithdrawTUSD = totalWithdrawTUSD.add(tusd);
            logger.info(closedOrder.getOrderId() + "(" + closedOrder.getPokketOrderId() + "):amount=" + amount + ",tusd=" + tusd);
        }
        logger.info("total withdraw tusd=" + totalWithdrawTUSD.toString());
        return totalWithdrawTUSD;
    }

    private void validateShouldReturnCollateral(List<PokketOrder> newOrders, BigDecimal totalWithdrawTUSD, BigDecimal totalDepositTUSD, String transactionHash, BigDecimal tusdTransfer) {
        if (!ethService.validateERC20Transaction(transactionHash,
                MAKKII_WALLET_ADDRESS,
                POKKET_ETH_WALLET_ADDRESS,
                TUSD,
                tusdTransfer.abs())) {
            for (PokketOrder order : newOrders) {
                order.setStatus(PokketOrderStatus.ERROR);
                order.setErrorMessage("invalid deposit tusd transaction hash: withdraw tusd=" + totalWithdrawTUSD
                        + ", deposit tusd=" + totalDepositTUSD);
                repo.save(order);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "invalid deposit tusd transaction hash: withdraw tusd=" + totalWithdrawTUSD
                            + ", deposit tusd=" + totalDepositTUSD);
        }
    }

    private void validateShouldDepositCollateral(List<PokketOrder> newOrders, BigDecimal totalWithdrawTUSD, BigDecimal totalDepositTUSD, String transactionHash, BigDecimal tusdTransfer) {
        if (!ethService.validateERC20Transaction(transactionHash,
                POKKET_ETH_WALLET_ADDRESS,
                MAKKII_WALLET_ADDRESS,
                TUSD,
                tusdTransfer.abs()
        )) {
            for (PokketOrder order : newOrders) {
                order.setStatus(PokketOrderStatus.ERROR);
                order.setErrorMessage("invalid deposit tusd transaction hash=" + transactionHash + ": withdraw tusd=" + totalWithdrawTUSD
                            + ", deposit tusd=" + totalDepositTUSD);
                repo.save(order);
            }

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "invalid deposit tusd transaction hash=" + transactionHash + ": withdraw tusd=" + totalWithdrawTUSD
                            + ", deposit tusd=" + totalDepositTUSD);
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
                // TODO:
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

    private boolean handleResultGreaterThan(String orderId, String txHashYieldTUSD) {
        PokketOrder order = getOrder(orderId);
        order.setResult(PokketOrderResult.GREATER_THAN);
        if (txHashYieldTUSD == null) {
            order.setStatus(PokketOrderStatus.ERROR);
            order.setErrorMessage("finish order failed: txHashYieldTUSD=" + txHashYieldTUSD);
            logger.error("finish order(" + orderId + ") failed: txHashYieldTUSD=" + txHashYieldTUSD);
            repo.save(order);
            return false;
        }
        BigDecimal expectedTUSD = PokketUtil.calculateCollateral(order.getAmount(),
                order.getToken2Collateral(),
                order.getWeeklyInterestRate());
        String expectedTo = order.getCollateralAddress() != null ? order.getCollateralAddress() : order.getInvestorAddress();
        // TODO: validate from/to
        if (!ethService.validateERC20Transaction(txHashYieldTUSD, POKKET_ETH_WALLET_ADDRESS, null/*expectedTo*/, TUSD, expectedTUSD)) {
            order.setStatus(PokketOrderStatus.ERROR);
            order.setErrorMessage("finish order(" + orderId + ") failed: Yield TUSD transaction(" + txHashYieldTUSD + ") is invalid");
            logger.error("finish order(" + orderId + ") failed: Yield TUSD transaction(" + txHashYieldTUSD + ") is invalid");
            return false;
        }
        order.setYieldTUSDTransactionHash(txHashYieldTUSD);
        order.setStatus(PokketOrderStatus.COMPLETE);
        repo.save(order);
        return true;
    }

    private boolean handleResultLessThanNoRoll(String orderId, String txHashYieldToken, String txHashReturnTUSD) {
        PokketOrder order = getOrder(orderId);
        order.setResult(PokketOrderResult.LESS_THAN_NO_ROLL);
        if (txHashYieldToken == null || txHashReturnTUSD == null) {
            order.setStatus(PokketOrderStatus.ERROR);
            order.setErrorMessage("finish order failed: txHashYieldToken=" +
                    txHashYieldToken + ", txHashReturnTUSD=" + txHashReturnTUSD);
            logger.error("finish order(" + orderId + ") failed: txHashYieldToken=" +
                    txHashYieldToken + ", txHashReturnTUSD=" + txHashReturnTUSD);
            repo.save(order);
            return false;
        }

        BigDecimal expectedAmount = order.getAmount().multiply(order.getWeeklyInterestRate().add(new BigDecimal(1)));
        if (order.getToken().equalsIgnoreCase(BTC)) {
            if (!btcService.validateBtcTransaction(
                    txHashYieldToken,
                    POKKET_BTC_WALLET_ADDRESS,
                    null/*order.getInvestorAddress()*/,
                    expectedAmount)) {
                order.setStatus(PokketOrderStatus.ERROR);
                order.setErrorMessage("Finish order(" + orderId + ") failed: Yield token transaction(" + txHashYieldToken + ") is invalid");
                logger.error("Yield token transaction(" + txHashYieldToken + ") is invalid");
                repo.save(order);
                return false;
            }
        } else if (order.getToken().equalsIgnoreCase(ETH)) {
            if (!ethService.validateEthTx(txHashYieldToken, POKKET_ETH_WALLET_ADDRESS, null/*order.getInvestorAddress()*/, expectedAmount)) {
                order.setStatus(PokketOrderStatus.ERROR);
                order.setErrorMessage("Finish order(" + orderId + ") failed: Yield token transaction(" + txHashYieldToken + ") is invalid");
                logger.error("Yield token transaction(" + txHashYieldToken + ") is invalid");
                repo.save(order);
                return false;
            }
        } else {
            if (!ethService.validateERC20Transaction(txHashYieldToken,
                    POKKET_ETH_WALLET_ADDRESS, null/*order.getInvestorAddress()*/, order.getToken(), expectedAmount)) {
                order.setStatus(PokketOrderStatus.ERROR);
                order.setErrorMessage("Finish Order(" + orderId + ") failed: Yield token transaction(" + txHashYieldToken + ") is invalid");
                logger.error("Finish Order(" + orderId + ") failed: Yield token transaction(" + txHashYieldToken + ") is invalid");
                repo.save(order);
                return false;
            }
        }
        BigDecimal expectedTUSD = PokketUtil.calculateCollateral(order.getAmount(),
                order.getToken2Collateral(),
                order.getWeeklyInterestRate());
        if (!ethService.validateERC20Transaction(txHashReturnTUSD, MAKKII_WALLET_ADDRESS, POKKET_ETH_WALLET_ADDRESS, TUSD, expectedTUSD)) {
            order.setStatus(PokketOrderStatus.ERROR);
            order.setErrorMessage("Finish order(" + orderId + ") failed: Return TUSD transaction(" + txHashReturnTUSD + ") is invalid");
            logger.error("Finish order(" + orderId + ") failed: Return TUSD transaction(" + txHashReturnTUSD + ") is invalid");
            repo.save(order);
            return false;
        }
        order.setYieldTokenTransactionHash(txHashYieldToken);
        order.setReturnTUSDTransactionHash(txHashReturnTUSD);
        order.setStatus(PokketOrderStatus.COMPLETE);
        repo.save(order);
        return true;
    }

    @ApiOperation(value="Get investors' orders")
    @GetMapping("/order")
    public Page<PokketOrder> getOrders(GetOrderReq getOrderReq) {
        List<String> addressList = getOrderReq.getAddresses();

        Sort sort = new Sort(Sort.Direction.DESC, "status", "createTime");
        Page<PokketOrder> orders = repo.findByInvestorAddressInAndStatusIsNot(addressList, PokketOrderStatus.ERROR,
                PageRequest.of(getOrderReq.getPage(), getOrderReq.getSize(), sort));
        return orders;
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
        } catch (PokketClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @ApiOperation(value="Get pokket's Bitcoin address which investors should transfer ETH/ERC20 to")
    @GetMapping("/deposit/btc_address")
    public String getDepositBtcAddress() {
        try {
            return pokketService.getDepositAddress(PokketService.ADDRESS_TYPE_BITCOIN);
        } catch (PokketClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @ApiOperation(value="Get pokket's ethereum address which investors should transfer ETH/ERC20 to")
    @GetMapping("/deposit/eth_address")
    public String getDepositEthAddress() {
        try {
            return pokketService.getDepositAddress(PokketService.ADDRESS_TYPE_ETH);
        } catch (PokketClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @ApiOperation(value="Get pokket financial banner images for advertisement use.",
        notes = "response is an array containing image download url")
    @GetMapping("/banners")
    public List<Banner> getBanners() {
        List<Banner> banners = new ArrayList<>();

        Banner b1 = new Banner();
        b1.setImageUrl("http://45.118.132.89/banner1.png");
        b1.setLink(null);
        banners.add(b1);

        Banner b2 = new Banner();
        b2.setImageUrl("http://45.118.132.89/banner2.png");
        b2.setLink(null);
        banners.add(b2);

        return banners;
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

    @GetMapping("orders")
    public List<PokketOrder> getOrdersByPage(@RequestBody QueryOrderReq req) {
        Long startTime = req.getStartTime();
        Long endTime = req.getEndTime();
        int page = req.getPage();
        int size = req.getSize();
        return repo.findByCreateTimeBetween(startTime, endTime, PageRequest.of(page, size));
    }

}
