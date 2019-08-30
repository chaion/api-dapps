package com.chaion.makkiiserver.blockchain;

import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("jsonrpc")
public class JsonRpcController {
    @Autowired
    BlockchainService blockchainService;

    @GetMapping("/eth/blocknumber")
    public BigInteger blockNumber() throws BlockchainException {
        return blockchainService.blockNumber();
    }
    @GetMapping("/eth/blockbynumber")
    public String blockByNumber(@RequestParam("number") BigInteger number) throws BlockchainException {
        EthBlock.Block block = blockchainService.getBlockByNumber(number);
        StringBuilder sb = new StringBuilder();
        sb.append("hash:" + block.getHash());
        sb.append("\r\nblock number:" + block.getNumber());
        sb.append("\r\nparent hash:" + block.getParentHash());
        sb.append("\r\ntransactions: ");
        List<EthBlock.TransactionResult> txs = block.getTransactions();
        for (EthBlock.TransactionResult txResult : txs) {
            if (txResult instanceof EthBlock.TransactionHash) {
                sb.append(((EthBlock.TransactionHash) txResult).get() + ",");
            } else if (txResult instanceof EthBlock.TransactionObject) {
                sb.append(((EthBlock.TransactionObject) txResult).getBlockHash() + ",");
            }
        }
        return sb.toString();
    }
    @GetMapping("/eth/transactionreceipt")
    public String transactionReceiptByHash(@RequestParam("txHash") String txHash) throws BlockchainException {
        TransactionReceipt receipt = blockchainService.getTransactionReceipt(txHash);
        JsonObject jo = new JsonObject();
        jo.addProperty("from", receipt.getFrom());
        jo.addProperty("to", receipt.getTo());
        if (receipt instanceof PlainTransactionReceipt) {
            jo.addProperty("status", ((PlainTransactionReceipt) receipt).getStatus());
            jo.addProperty("blocknumber", ((PlainTransactionReceipt) receipt).getBlockNumberString());
            jo.addProperty("transactionhash", receipt.getBlockHash());
        } else {
            jo.addProperty("status", receipt.getStatus());
            jo.addProperty("blocknumber", receipt.getBlockNumber());
            jo.addProperty("transactionhash", receipt.getTransactionHash());
        }
        return jo.toString();
    }
    @GetMapping("/eth/transactionbyhash")
    public String transactionByHash(@RequestParam("txHash") String txHash) throws BlockchainException {
        Transaction transaction = blockchainService.getTransaction(txHash);
        JsonObject jo = new JsonObject();
        jo.addProperty("value", transaction.getValue());
        jo.addProperty("txHash", transaction.getHash());
        jo.addProperty("input", transaction.getInput());
        return jo.toString();
    }
    @GetMapping("/eth/getbalance")
    public BigInteger getBalance(@RequestParam("address") String address) throws BlockchainException {
        return blockchainService.getBalance(address);
    }

    @PostMapping("/eth/sendrawtransaction")
    public String sendRawTransaction(@RequestBody String rawTransaction) throws BlockchainException {
        return blockchainService.sendRawTransaction(rawTransaction);
    }
}
