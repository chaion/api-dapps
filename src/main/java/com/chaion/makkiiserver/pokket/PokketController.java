package com.chaion.makkiiserver.pokket;

import com.chaion.makkiiserver.blockchain.BlockchainException;
import com.chaion.makkiiserver.pokket.model.*;
import com.chaion.makkiiserver.pokket.repository.PokketOrderRepository;
import com.chaion.makkiiserver.blockchain.BlockchainService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.chaion.makkiiserver.pokket.PokketUtil.MAKKII_WALLET_ADDRESS;
import static com.chaion.makkiiserver.pokket.PokketUtil.POKKET_WALLET_ADDRESS;

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
        logger.info("[pokket] new order id=" + orderId);

        String rawTransaction = req.getRawTransaction();
        String txHash = null;
        try {
            txHash = blockchainService.sendRawTransaction(rawTransaction);
            logger.info("[pokket] sending invest raw transaction: txHash=" + txHash);
            blockchainService.addPendingTransaction(txHash, (transactionHash, status) -> {
                PokketOrder order = getOrder(orderId);
                if (status) {
                    logger.info("[pokket] invest transaction is confirmed, update status to WAIT_COLLATERAL_DEPOSIT");
                    order.setStatus(PokketOrderStatus.WAIT_COLLATERAL_DEPOSIT);
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

        long currentTime = System.currentTimeMillis();
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

    @PostMapping("/order/status/collateralDeposited")
    public void collateralDeposited(@RequestParam(value="orderId") String orderId,
                                    @RequestParam(value="txHash") String txHash,
                                    @RequestParam(value="token2Collateral") BigDecimal token2TUSD) {
        PokketOrder order = getOrder(orderId);

        // check transaction
        BigDecimal expectedTUSD = PokketUtil.calculateCollateral(order.getAmount(), token2TUSD, order.getWeeklyInterestRate());
        if (!blockchainService.validateERC20Transaction(txHash, POKKET_WALLET_ADDRESS, MAKKII_WALLET_ADDRESS,
                "TUSD", expectedTUSD)) {
            order.setStatus(PokketOrderStatus.ERROR);
            order.setDepositTUSDTransactionHash(txHash);
            order.setToken2Collateral(token2TUSD);
            repo.save(order);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TUSD deposit transaction(" + txHash + ") invalid");
        }

        order.setToken2Collateral(token2TUSD);
        order.setStatus(PokketOrderStatus.IN_PROGRESS);
        order.setDepositTUSDTransactionHash(txHash);
        order.setStartTime(System.currentTimeMillis());
        repo.save(order);
    }

    @PostMapping("/order/status/complete")
    public void finishOrder(@RequestParam(value="orderId") String orderId,
                            @RequestParam(value="result") PokketOrderResult result,
                            @RequestParam(value="txHashYieldToken", required = false) String txHashYieldToken,
                            @RequestParam(value="txHashReturnTUSD", required = false) String txHashReturnTUSD,
                            @RequestParam(value="txHashYieldTUSD", required = false) String txHashYieldTUSD) {
        if (result == PokketOrderResult.LESS_THAN_NO_ROLL) {
            handleResultLessThanNoRoll(orderId, txHashYieldToken, txHashReturnTUSD);
        } else if (result == PokketOrderResult.LESS_THAN_ROLL) {
            // TODO:
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
        } else if (result == PokketOrderResult.GREATER_THAN) {
            handleResultGreaterThan(orderId, txHashYieldTUSD);
        }
    }

    private void handleResultGreaterThan(String orderId, String txHashYieldTUSD) {
        PokketOrder order = getOrder(orderId);
        order.setResult(PokketOrderResult.GREATER_THAN);
        if (txHashYieldTUSD == null) {
            order.setStatus(PokketOrderStatus.ERROR);
            order.setErrorMessage("finish order failed: txHashYieldTUSD=" + txHashYieldTUSD);
            repo.save(order);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing parameter txHashYieldTUSD");
        }
        BigDecimal expectedTUSD = PokketUtil.calculateCollateral(order.getAmount(),
                order.getToken2Collateral(),
                order.getWeeklyInterestRate());
        if (order.getToken().equalsIgnoreCase("BTC")) {
            // TODO: handle BTC
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "btc investment is not implemented.");
        } else {
            String expectedTo = order.getCollateralAddress() != null ? order.getCollateralAddress() : order.getInvestorAddress();
            if (!blockchainService.validateERC20Transaction(txHashYieldTUSD, POKKET_WALLET_ADDRESS, expectedTo, "TUSD", expectedTUSD)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yield TUSD transaction(" + txHashYieldTUSD + ") is invalid");
            }
        }
        order.setYieldTUSDTransactionHash(txHashYieldTUSD);
        order.setStatus(PokketOrderStatus.COMPLETE);
        repo.save(order);
    }

    private void handleResultLessThanNoRoll(String orderId, String txHashYieldToken, String txHashReturnTUSD) {
        PokketOrder order = getOrder(orderId);
        order.setResult(PokketOrderResult.LESS_THAN_NO_ROLL);
        if (txHashYieldToken == null || txHashReturnTUSD == null) {
            order.setStatus(PokketOrderStatus.ERROR);
            order.setErrorMessage("finish order failed: txHashYieldToken=" +
                    txHashYieldToken + ", txHashReturnTUSD=" + txHashReturnTUSD);
            repo.save(order);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Missing parameter txHashYieldToken or txHashReturnTUSD");
        }

        BigDecimal expectedAmount = order.getAmount().multiply(order.getWeeklyInterestRate().add(new BigDecimal(1)));
        if (order.getToken().equalsIgnoreCase("BTC")) {
            // TODO:
        } else if (order.getToken().equalsIgnoreCase("ETH")) {
            if (!blockchainService.validateEthTx(txHashYieldToken, POKKET_WALLET_ADDRESS, order.getInvestorAddress(), expectedAmount)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Yield token transaction(" + txHashYieldToken + ") is invalid");
            }
        } else {
            if (!blockchainService.validateERC20Transaction(txHashYieldToken,
                    POKKET_WALLET_ADDRESS, order.getInvestorAddress(), order.getToken(), expectedAmount)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Yield token transaction(" + txHashYieldToken + ") is invalid");
            }
        }
        BigDecimal expectedTUSD = PokketUtil.calculateCollateral(order.getAmount(),
                order.getToken2Collateral(),
                order.getWeeklyInterestRate());
        if (!blockchainService.validateERC20Transaction(txHashReturnTUSD, MAKKII_WALLET_ADDRESS, POKKET_WALLET_ADDRESS, "TUSD", expectedTUSD)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Return TUSD transaction(" + txHashReturnTUSD + ") is invalid");
        }
        order.setYieldTokenTransactionHash(txHashYieldToken);
        order.setReturnTUSDTransactionHash(txHashReturnTUSD);
        order.setStatus(PokketOrderStatus.COMPLETE);
        repo.save(order);
    }

    @GetMapping("/order")
    public Page<PokketOrder> getOrders(@RequestParam(value="addresses") String addresses,
                                       @RequestParam(value="page") int page,
                                       @RequestParam(value="size") int size) {
        List<String> addressList = Arrays.asList(addresses.split(","));

        Sort sort = new Sort(Sort.Direction.DESC, "status", "createTime");
        Page<PokketOrder> orders = repo.findByInvestorAddressInAndStatusIsNot(addressList, PokketOrderStatus.ERROR,
                PageRequest.of(page, size, sort));
        return orders;
    }

    @GetMapping("/product")
    public List<PokketProduct> getProducts(@RequestParam(value="search", required = false) String search) {
        if (search == null || search.isEmpty()) {
            return pokketService.getProducts();
        } else {
            return pokketService.searchProducts(search);
        }
    }

    @GetMapping("/statistic/totalInvestment")
    public BigDecimal getTotalInvestment() {
        return pokketService.getTotalInvestment();
    }

    @GetMapping("/deposit/btc_address")
    public String getDepositBtcAddress() {
        return pokketService.getDepositAddress(PokketService.ADDRESS_TYPE_BITCOIN);
    }

    @GetMapping("/deposit/eth_address")
    public String getDepositEthAddress() {
        return pokketService.getDepositAddress(PokketService.ADDRESS_TYPE_ETH);
    }

    @GetMapping("/banners")
    public String[] getBanners() {
        return new String[] {
            "http://45.118.132.89/banner1.png",
            "http://45.118.132.89/banner2.png"
        };
    }

    private PokketOrder getOrder(String orderId) {
        Optional<PokketOrder> orderOpt = repo.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order(" + orderId + ") is not found.");
        }
        return orderOpt.get();
    }

}
