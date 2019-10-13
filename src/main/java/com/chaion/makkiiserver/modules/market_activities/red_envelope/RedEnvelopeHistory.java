package com.chaion.makkiiserver.modules.market_activities.red_envelope;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;

import java.math.BigInteger;
import java.util.Date;

@Data
public class RedEnvelopeHistory {
    private String phoneId;
    private String address;
    private BigInteger amount;
    @CreatedBy
    private Date time;
    private String txHash;
}
