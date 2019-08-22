package com.chaion.makkiiserver.model;

import io.swagger.annotations.ApiModelProperty;

public class DefaultConfig {
    @ApiModelProperty(value="kyber wallet id", example="0x4BE78b8BA92567AB0889BE896578Be56B816D318")
    private String kyberWalletId;

    private String[] supportedModule;

    public String getKyberWalletId() {
        return kyberWalletId;
    }

    public void setKyberWalletId(String kyberWalletId) {
        this.kyberWalletId = kyberWalletId;
    }

    public String[] getSupportedModule() {
        return supportedModule;
    }

    public void setSupportedModule(String[] supportedModule) {
        this.supportedModule = supportedModule;
    }
}
