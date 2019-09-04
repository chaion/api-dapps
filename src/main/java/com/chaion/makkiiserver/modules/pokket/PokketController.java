package com.chaion.makkiiserver.modules.pokket;

import com.chaion.makkiiserver.blockchain.BlockchainException;
import com.chaion.makkiiserver.modules.pokket.model.*;
import com.chaion.makkiiserver.modules.pokket.repository.PokketOrderRepository;
import com.chaion.makkiiserver.blockchain.BlockchainService;
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

import javax.validation.Valid;
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
    @Autowired BlockchainService blockchainService;

    @PutMapping("/order")
    public PokketOrder createOrder(@Valid @RequestBody CreateOrderReq req) {
        final String orderId = PokketUtil.generateOrderId();
        logger.info("[pokket] receive new order request, generate order id=" + orderId);
        long currentTime = System.currentTimeMillis();

        String rawTransaction = req.getRawTransaction();
        String txHash = null;
        try {
            txHash = blockchainService.sendRawTransaction(rawTransaction);
            logger.info("[pokket] sending invest raw transaction: txHash=" + txHash);
            blockchainService.addPendingTransaction(txHash, (transactionHash, status) -> {
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
        order.setInvestTransactionHash(txHash);
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
        List<PokketOrder> newOrders = repo.findByOrderIdIn(collateralSettlementReq.getNewOrderIds());

        BigDecimal totalWithdrawTUSD = new BigDecimal("0");
        // calculate collateral net
        for (PokketOrder closedOrder : closedOrders) {
            BigDecimal amount = closedOrder.getAmount();
            BigDecimal token2Collateral = closedOrder.getToken2Collateral();
            BigDecimal weeklyInterest = closedOrder.getWeeklyInterestRate();
            BigDecimal tusd = calculateCollateral(amount, token2Collateral, weeklyInterest);
            totalWithdrawTUSD.add(tusd);
        }
        logger.info("total withdraw tusd=" + totalWithdrawTUSD.toString());
        Map<String, BigDecimal> ratios = collateralSettlementReq.getToken2CollateralMap();
        BigDecimal totalDepositTUSD = new BigDecimal("0");
        for (PokketOrder newOrder : newOrders) {
            newOrder.setDepositTUSDTransactionHash(collateralSettlementReq.getTransactionHash());
            newOrder.setToken2Collateral(ratios.get(newOrder.getToken()));
            BigDecimal amount = newOrder.getAmount();
            BigDecimal token2Collateral = newOrder.getToken2Collateral();
            BigDecimal weeklyInterest = newOrder.getWeeklyInterestRate();
            BigDecimal tusd = calculateCollateral(amount, token2Collateral, weeklyInterest);
            totalDepositTUSD.add(tusd);
        }
        logger.info("total deposit tusd=" + totalDepositTUSD.toString());
        String transactionHash = collateralSettlementReq.getTransactionHash();
        // validate transaction
        BigDecimal tusdTransfer = totalDepositTUSD.subtract(totalWithdrawTUSD);
        if (tusdTransfer.signum() > 0) {
            if (!blockchainService.validateERC20Transaction(transactionHash,
                    POKKET_WALLET_ADDRESS,
                    MAKKII_WALLET_ADDRESS,
                    TUSD,
                    tusdTransfer.abs()
            )) {
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
        } else if (tusdTransfer.signum() < 0) {
            if (!blockchainService.validateERC20Transaction(transactionHash,
                    MAKKII_WALLET_ADDRESS,
                    POKKET_WALLET_ADDRESS,
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
        } else {
            logger.info("tusd withdraw and deposit are equal, no transaction required.");
        }

        for (PokketOrder newOrder : newOrders) {
            newOrder.setStatus(PokketOrderStatus.IN_PROGRESS);
            newOrder.setStartTime(System.currentTimeMillis());
            repo.save(newOrder);
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
        if (order.getToken().equalsIgnoreCase("BTC")) {
            // TODO: handle BTC
            return false;
        } else {
            String expectedTo = order.getCollateralAddress() != null ? order.getCollateralAddress() : order.getInvestorAddress();
            // TODO: validate from/to
            if (!blockchainService.validateERC20Transaction(txHashYieldTUSD, POKKET_WALLET_ADDRESS, null/*expectedTo*/, TUSD, expectedTUSD)) {
                order.setStatus(PokketOrderStatus.ERROR);
                order.setErrorMessage("finish order(" + orderId + ") failed: Yield TUSD transaction(" + txHashYieldTUSD + ") is invalid");
                logger.error("finish order(" + orderId + ") failed: Yield TUSD transaction(" + txHashYieldTUSD + ") is invalid");
                return false;
            }
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
        if (order.getToken().equalsIgnoreCase("BTC")) {
            // TODO:
            return false;
        } else if (order.getToken().equalsIgnoreCase("ETH")) {
            if (!blockchainService.validateEthTx(txHashYieldToken, POKKET_WALLET_ADDRESS, null/*order.getInvestorAddress()*/, expectedAmount)) {
                order.setStatus(PokketOrderStatus.ERROR);
                order.setErrorMessage("Finish order(" + orderId + ") failed: Yield token transaction(" + txHashYieldToken + ") is invalid");
                logger.error("Yield token transaction(" + txHashYieldToken + ") is invalid");
                repo.save(order);
                return false;
            }
        } else {
            if (!blockchainService.validateERC20Transaction(txHashYieldToken,
                    POKKET_WALLET_ADDRESS, null/*order.getInvestorAddress()*/, order.getToken(), expectedAmount)) {
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
        if (!blockchainService.validateERC20Transaction(txHashReturnTUSD, MAKKII_WALLET_ADDRESS, POKKET_WALLET_ADDRESS, TUSD, expectedTUSD)) {
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
        if (search == null || search.isEmpty()) {
            return pokketService.getProducts();
        } else {
            return pokketService.searchProducts(search);
        }
    }

    @ApiOperation(value="Get pokket's total deposit amount, including deposits on pokket's website")
    @GetMapping("/statistic/totalInvestment")
    public BigDecimal getTotalInvestment() {
        return pokketService.getTotalInvestment();
    }

    @ApiOperation(value="Get pokket's Bitcoin address which investors should transfer ETH/ERC20 to")
    @GetMapping("/deposit/btc_address")
    public String getDepositBtcAddress() {
        return pokketService.getDepositAddress(PokketService.ADDRESS_TYPE_BITCOIN);
    }

    @ApiOperation(value="Get pokket's ethereum address which investors should transfer ETH/ERC20 to")
    @GetMapping("/deposit/eth_address")
    public String getDepositEthAddress() {
        return pokketService.getDepositAddress(PokketService.ADDRESS_TYPE_ETH);
    }

    @ApiOperation(value="Get pokket financial banner images for advertisement use.",
        notes = "response is an array containing image download url")
    @GetMapping("/banners")
    public String[] getBanners() {
        return new String[] {
            "http://45.118.132.89/banner1.png",
            "http://45.118.132.89/banner2.png"
        };
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

}
