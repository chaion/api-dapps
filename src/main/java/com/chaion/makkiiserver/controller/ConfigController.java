package com.chaion.makkiiserver.controller;

import com.chaion.makkiiserver.model.DefaultConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value="Dynamic configurations for app", description="Allow app to dynamic load configurations without upgrade app package")
@RestController
@RequestMapping("config")
public class ConfigController {

    @ApiOperation(value="Load default configurations",
        response=DefaultConfig.class)
    @GetMapping
    public DefaultConfig getDefaultConfig() {
        DefaultConfig config = new DefaultConfig();
        config.setKyberWalletId("0x4BE78b8BA92567AB0889BE896578Be56B816D318");
        config.setSupportedModule(new String[] {"Pokket"});
        return config;
    }
}
