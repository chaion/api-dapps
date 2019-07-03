package com.chaion.makkiiserver.model.analytics;

import java.util.Map;

public class UserInfo {
    private long startTime;
    private long endTime;
    private Map<String, Long> userCountsByPlatform;
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
