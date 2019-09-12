package com.chaion.makkiiserver.modules.config;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleConfigRepository extends MongoRepository<ModuleConfig, String> {

    ModuleConfig findFirstByModuleNameIgnoreCase(String moduleName);

}
