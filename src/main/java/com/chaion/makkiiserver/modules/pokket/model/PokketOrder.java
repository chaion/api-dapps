package com.chaion.makkiiserver.modules.pokket.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Data
public class PokketOrder {

    @Id
    private String orderId;
    private Long pokketOrderId;
    private String investorAddress;
    private String collateralAddress;
    private String token;
    private String tokenFullName;
    private BigDecimal amount;
    private BigDecimal token2Collateral;
    private BigDecimal weeklyInterestRate;
    private BigDecimal yearlyInterestRate;
    private String investTransactionHash;
    private String depositTUSDTransactionHash;
    private String yieldTokenTransactionHash;
    private String returnTUSDTransactionHash;
    private String yieldTUSDTransactionHash;
    private Long createTime;
    private PokketOrderStatus status;
    private PokketOrderResult result;
    private Long startTime;
    private Long productId;
    private String previousOrderId;
    private String errorMessage;
}
