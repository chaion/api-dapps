package com.chaion.makkiiserver.model.analytics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(description = "Transfer statistics model")
public class TransferInfo {

    @ApiModelProperty(value="total number of transactions within the given period",
        example = "3")
    private long totalCount;

    @ApiModelProperty(value="when does transfer statistic starts from", example = "1562142132139")
    private long startTime;

    @ApiModelProperty(value="when does transfer statistic ends at", example = "1562147658022")
    private long endTime;

    @ApiModelProperty(value="transfer statistic of each coin/token")
    private List<CoinTransferInfo> coinTransferList;

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

    public List<CoinTransferInfo> getCoinTransferList() {
        return coinTransferList;
    }

    public void setCoinTransferList(List<CoinTransferInfo> coinTransferList) {
        this.coinTransferList = coinTransferList;
    }
}
