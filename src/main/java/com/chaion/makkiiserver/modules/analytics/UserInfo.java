package com.chaion.makkiiserver.modules.analytics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

@ApiModel(description = "User statistic model")
public class UserInfo {

    @ApiModelProperty(value="when does user statistic starts from", example = "1562142132139")
    private long startTime;

    @ApiModelProperty(value="when does user statistic ends at", example = "1562147658022")
    private long endTime;

    @ApiModelProperty(value="registered user count for each platform", example = "{\"ANDROID\": 2, \"IOS\": 0}")
    private Map<String, Long> userCountsByPlatform;

    @ApiModelProperty(value="total number of users that are registered within the given period", example = "2")
    private long total;

    public Map<String, Long> getUserCountsByPlatform() {
        return userCountsByPlatform;
    }

    public void setUserCountsByPlatform(Map<String, Long> userCountsByPlatform) {
        this.userCountsByPlatform = userCountsByPlatform;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
