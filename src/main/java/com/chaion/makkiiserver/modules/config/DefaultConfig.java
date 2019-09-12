package com.chaion.makkiiserver.modules.config;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class DefaultConfig {
    @ApiModelProperty(value="kyber wallet id", example="0x4BE78b8BA92567AB0889BE896578Be56B816D318")
    private String kyberWalletId;

    private List<String> supportedModule;
}
