package com.chaion.makkiiserver.modules.config;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Api(value="Dynamic configurations for app", description="Allow app to dynamic load configurations without upgrade app package")
@RestController
@RequestMapping("config")
public class ConfigController {
    @Autowired
    ModuleConfigRepository repo;

    @PutMapping
    public void addModule(@RequestBody ModuleConfig moduleConfig) {
        repo.save(moduleConfig);
    }

    @PostMapping("/{module}/{enable}")
    public void enableModule(@PathVariable("module") String module, @PathVariable("enable") String enable) {
        ModuleConfig config = repo.findFirstByModuleNameIgnoreCase(module);
        boolean enabled = enable.toLowerCase().contains("enable");
        config.setEnabled(enabled);
        repo.save(config);
    }

    @PostMapping("/{module}")
    public void updateModuleParam(@PathVariable("module") String module, @RequestBody Map<String, String> params) {
        ModuleConfig config = repo.findFirstByModuleNameIgnoreCase(module);
        config.setModuleParams(params);
        repo.save(config);
    }

    @ApiOperation(value="Load default configurations", response=DefaultConfig.class)
    @GetMapping
    public DefaultConfig getDefaultConfig() {
        String kyberWalletId = "0x4BE78b8BA92567AB0889BE896578Be56B816D318";
        List<String> supportedModules = new ArrayList<>();

        List<ModuleConfig> list = repo.findAll();
        for (ModuleConfig module : list) {
            if (module.isEnabled()) {
                supportedModules.add(module.getModuleName());
                if (module.getModuleName().equalsIgnoreCase("kyber")) {
                    Map<String, String> kyberParam = module.getModuleParams();
                    if (kyberParam.containsKey("wallet_id")) {
                        kyberWalletId = kyberParam.get("wallet_id");
                    }
                }
            }
        }

        DefaultConfig config = new DefaultConfig();
        config.setKyberWalletId(kyberWalletId);
        config.setSupportedModule(supportedModules);
        return config;
    }
}
