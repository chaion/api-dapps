package com.chaion.makkiiserver.modules.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Map;


@ApiModel(description="Module Configurations")
@Data
public class ModuleConfig {

    @Id
    private String moduleId;

    @ApiModelProperty(value = "module name")
    private String moduleName;

    @ApiModelProperty(value = "module parameters", allowEmptyValue = true)
    private Map<String, String> moduleParams;

    @ApiModelProperty(value = "module status, if it is enabled")
    private boolean enabled;
}
