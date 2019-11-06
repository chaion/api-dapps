package com.chaion.makkiiserver.modules.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {

    @Autowired
    ModuleConfigRepository moduleRepo;

    public ModuleConfig getModule(String moduleName) {
        return moduleRepo.findFirstByModuleNameIgnoreCase(moduleName);
    }
}
