package com.chaion.makkiiserver.model.analytics;

import java.util.Map;

public class TransferInfo {
    private long totalCount;
    private long startTime;
    private long endTime;
    private Map<String, Long> transferCountMap;
    private Map<String, Double> transferAmountMap;

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
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

    public Map<String, Long> getTransferCountMap() {
        return transferCountMap;
    }

    public void setTransferCountMap(Map<String, Long> transferCountMap) {
        this.transferCountMap = transferCountMap;
    }

    public Map<String, Double> getTransferAmountMap() {
        return transferAmountMap;
    }

    public void setTransferAmountMap(Map<String, Double> transferAmountMap) {
        this.transferAmountMap = transferAmountMap;
    }
}
