package com.chaion.makkiiserver.modules.blockchain.transaction;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class SimpleTransaction {
    @Id
    private String id;
    private String txHash;
    private String chain;
    private String note;
}
