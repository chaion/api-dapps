package com.chaion.makkiiserver.modules.blockchain.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("transaction")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    @Autowired
    TransactionRepository txRepo;

    @PostMapping
    public SimpleTransaction updateTransaction(@RequestBody SimpleTransaction req) {
        SimpleTransaction tx = txRepo.findFirstByChainAndTxHashAndAddress(req.getChain(), req.getTxHash(), req.getAddress());
        if (tx != null) {
            tx.setNote(req.getNote());
            return txRepo.save(tx);
        } else {
            return txRepo.save(req);
        }
    }

    @GetMapping
    public SimpleTransaction getTransaction(@RequestParam("chain") String chain,
                                            @RequestParam("txHash") String txHash,
                                            @RequestParam("address") String address) {
        SimpleTransaction tx = txRepo.findFirstByChainAndTxHashAndAddress(chain, txHash, address);
        if (tx != null) return tx;
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
