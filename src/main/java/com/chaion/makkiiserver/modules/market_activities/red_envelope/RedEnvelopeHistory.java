package com.chaion.makkiiserver.modules.market_activities.red_envelope;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;

import java.math.BigInteger;
import java.util.Date;

@Data
public class RedEnvelopeHistory {
    @Id
    private String id;
    private String phoneId;
    private String address;
    private BigInteger amount;
    @CreatedBy
    private Date time;
    private String txHash;
    private RedEnvelopeHsitoryStatus status;
}
