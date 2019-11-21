package com.chaion.makkiiserver.modules.config;

import com.chaion.makkiiserver.Utils;
import com.chaion.makkiiserver.repository.file.StorageException;
import com.chaion.makkiiserver.repository.file.StorageService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Api(value="Dynamic configurations for app", description="Allow app to dynamic load configurations without upgrade app package")
@RestController
@RequestMapping("config")
public class ConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    ModuleConfigRepository repo;

    @PreAuthorize("hasRole('ROLE_MAKKII') or hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/apiServers")
    @ResponseBody
    public String apiServerConfig() {
        // TODO: optimize: be able to update api servers without restarting server
        try {
            ClassPathResource classPathResource = new ClassPathResource("api_servers.json");
            InputStream in = classPathResource.getInputStream();
            return Utils.inputStream2String(in);
        } catch (IOException e) {
            logger.error("load file failed: ", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "api_servers.json not found");
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/modules")
    public void addModule(@RequestBody ModuleConfig moduleConfig) {
        repo.save(moduleConfig);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/{module}/{enable}")
    public void enableModule(@PathVariable("module") String module, @PathVariable("enable") String enable) {
        ModuleConfig config = repo.findFirstByModuleNameIgnoreCase(module);
        boolean enabled = enable.toLowerCase().contains("enable");
        config.setEnabled(enabled);
        repo.save(config);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/{module}")
    public void updateModuleParam(@PathVariable("module") String module, @RequestBody Map<String, String> params) {
        ModuleConfig config = repo.findFirstByModuleNameIgnoreCase(module);
        config.setModuleParams(params);
        repo.save(config);
    }

    @PreAuthorize("hasRole('ROLE_MAKKII') or hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/modules", produces = "application/json; charset=utf-8")
    public String getModules() {
        JsonArray modules = new JsonArray();
        List<ModuleConfig> configs = repo.findAll();
        for (ModuleConfig config : configs) {
            JsonObject module = new JsonObject();
            module.addProperty("moduleId", config.getModuleId());
            module.addProperty("moduleName", config.getModuleName());
            module.addProperty("enabled", config.isEnabled());
            Map<String, String> map = config.getModuleParams();
            if (map != null) {
                JsonObject params = new JsonObject();
                module.add("moduleParams", params);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    try {
                        JsonElement e = new JsonParser().parse(entry.getValue());
                        if (e.isJsonObject() || e.isJsonArray()) {
                            params.add(entry.getKey(), e);
                        } else {
                            params.addProperty(entry.getKey(), entry.getValue());
                        }
                    } catch (Exception e) {
                        params.addProperty(entry.getKey(), entry.getValue());
                    }
                }
            }
            modules.add(module);
        }
        return modules.toString();
    }

    @PreAuthorize("hasRole('ROLE_MAKKII')")
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
