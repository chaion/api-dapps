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
    public void updateTransaction(@RequestBody SimpleTransaction req) {
        SimpleTransaction tx = txRepo.findFirstByChainAndTxHash(req.getChain(), req.getTxHash());
        if (tx != null) {
            tx.setNote(req.getNote());
        }
        txRepo.save(tx);
    }

    @GetMapping
    public SimpleTransaction getTransaction(@RequestBody SimpleTransaction req) {
        SimpleTransaction tx = txRepo.findFirstByChainAndTxHash(req.getChain(), req.getTxHash());
        if (tx != null) return tx;
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
