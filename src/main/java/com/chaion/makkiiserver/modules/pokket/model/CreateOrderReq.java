package com.chaion.makkiiserver.modules.pokket.model;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreateOrderReq {
    @NotEmpty
    private String investorAddress;
    @NotEmpty
    private String token;
    private String tokenFullName;
    @NotNull
    private BigDecimal amount;
    private String collateralAddress;
    @NotNull
    private BigDecimal weeklyInterestRate;
    @NotNull
    private BigDecimal yearlyInterestRate;
    @NotNull
    private BigDecimal token2Collateral;
    private boolean autoRoll;
    /**
     * token/tokenfullname/weeklyInterestRate/yearlyInterestRate are included in product id.
     */
    @NotNull
    private Long productId;
    @NotEmpty
    private String rawTransaction;
}
