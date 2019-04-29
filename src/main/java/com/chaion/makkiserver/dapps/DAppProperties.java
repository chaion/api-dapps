package com.chaion.makkiserver.dapps;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("dapp")
public class DAppProperties {
    private long maxPkgSize;

    public long getMaxPkgSize() {
        return maxPkgSize;
    }

    public void setMaxPkgSize(long maxPkgSize) {
        this.maxPkgSize = maxPkgSize;
    }
}
