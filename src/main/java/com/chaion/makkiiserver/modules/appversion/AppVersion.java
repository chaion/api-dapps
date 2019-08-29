package com.chaion.makkiiserver.modules.appversion;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;

import java.util.Map;

@ApiModel(description="All details about app version including both iOS and Android")
public class AppVersion {
    @Id
    private String id;

    @ApiModelProperty(value="displayed version", example = "1.0.0")
    private String version;

    @ApiModelProperty(value="Used this field to differentiate different versions", example = "10")
    private Integer versionCode;

    @ApiModelProperty(allowableValues = "iOS, Android", example = "Android")
    private String platform;

    @ApiModelProperty(value="mandatory means all previous app version under the same platform " +
            "should not be used any more.")
    private Boolean mandatory;

    @ApiModelProperty(value="key is language abbr, value is update message.",
            example = "{ \"en\" : \"1.Support Bitcoin, Litecoin, Ethereum and Tron " +
                    "2. Support Aion Token Standard, ERC20 Tokens " +
                    "3. Support TouchID and PIN Code Authentication\", " +
                    "\"zh\" : \"1. 新增比特币、莱特币、以太坊、波场币支持 " +
                    "2. 支持Aion Token Standard、ERC20 Tokens " +
                    "3. 增加指纹、PIN码认证方式\" }")
    private Map<String, String> updatesMap;

    @ApiModelProperty(value="Downloadable url of Android apk[Only for Android]",
            allowEmptyValue = true,
            example = "http://45.118.132.89/Makkii_testnet_1.0.0.apk"
    )
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public Map<String, String> getUpdatesMap() {
        return updatesMap;
    }

    public void setUpdatesMap(Map<String, String> updatesMap) {
        this.updatesMap = updatesMap;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "AppVersion{" +
                "id='" + id + '\'' +
                ", version='" + version + '\'' +
                ", versionCode=" + versionCode +
                ", platform='" + platform + '\'' +
                ", mandatory=" + mandatory +
                ", updatesMap=" + updatesMap +
                ", url='" + url + '\'' +
                '}';
    }
}
