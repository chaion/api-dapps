package com.chaion.makkiiserver.modules.pokket;

import com.chaion.makkiiserver.blockchain.BlockchainException;
import com.chaion.makkiiserver.blockchain.eth.EthService;
import com.chaion.makkiiserver.blockchain.eth.PlainTransactionReceipt;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;

/**
 * This is only for pokket's private chain jsonrpc
 */
@RestController
@RequestMapping("pokketchain")
public class JsonRpcController {
    @Autowired
    EthService ethService;

    @PostMapping
    public String jsonrpc(@RequestBody String payload) throws BlockchainException {
        JsonObject root = new JsonParser().parse(payload).getAsJsonObject();

        JsonObject resp = new JsonObject();
        resp.addProperty("id", 1);
        resp.addProperty("jsonrpc", "2.0");

        String method = root.get("method").getAsString();
        if (method.equalsIgnoreCase("eth_getTransactionCount")) {
            JsonArray array = root.get("params").getAsJsonArray();
            String address = array.get(0).getAsString();
            BigInteger count = ethService.getTransactionCount(address);
            resp.addProperty("result", "0x" + count.toString(16));
            return resp.toString();
        } else if (method.equalsIgnoreCase("eth_blockNumber")) {
            BigInteger blocknumber = ethService.blockNumber();
            resp.addProperty("result", "0x" + blocknumber.toString(16));
            return resp.toString();
        } else if (method.equalsIgnoreCase("eth_getBalance")) {
            JsonArray array = root.get("params").getAsJsonArray();
            String address = array.get(0).getAsString();
            BigInteger balance = ethService.getBalance(address);
            resp.addProperty("result", "0x" + balance.toString(16));
            return resp.toString();
        } else if (method.equalsIgnoreCase("eth_sendRawTransaction")) {
            JsonArray array = root.get("params").getAsJsonArray();
            String raw = array.get(0).getAsString();
            String txHash = ethService.sendRawTransaction(raw);
            resp.addProperty("result", txHash);
            return resp.toString();
        } else if (method.equalsIgnoreCase("eth_getBlockByNumber")) {
            JsonArray array = root.get("params").getAsJsonArray();
            BigInteger n;
            String number = array.get(0).getAsString();
            if (number.startsWith("0x")) {
                number = number.substring(2);
                n = new BigInteger(number, 16);
            } else {
                n = new BigInteger("" + number);
            }
            EthBlock.Block block = ethService.getBlockByNumber(n);
            JsonObject result = new JsonObject();
//            result.addProperty("number", "0x" + block.getNumber().toString(16));
//            result.addProperty("hash", block.getHash());
//            result.addProperty("parentHash", block.getParentHash());
//            result.addProperty("nonce", "0x" + block.getNonce().toString(16));
//            result.addProperty("sha3Uncles", block.getSha3Uncles());
//            result.addProperty("logsBloom", block.getLogsBloom());
//            result.addProperty("transactionsRoot", block.getTransactionsRoot());
//            result.addProperty("stateRoot", block.getStateRoot());
//            result.addProperty("miner", block.getMiner());
//            result.addProperty("difficulty", "0x" + block.getDifficulty().toString(16));
//            result.addProperty("totalDifficulty", "0x" + block.getTotalDifficulty().toString(16));
//            result.addProperty("extraData", block.getExtraData());
//            result.addProperty("size", "0x" + block.getSize().toString(16));
//            result.addProperty("gasLimit", "0x" + block.getGasLimit().toString(16));
//            result.addProperty("gasUsed", "0x" + block.getGasUsed().toString(16));
            result.addProperty("timestamp", "0x" + block.getTimestamp().toString(16));
            List<String> uncles = block.getUncles();
            JsonArray unclesArray = new JsonArray();
            for (String uncle : uncles) {
                unclesArray.add(uncle);
            }
            result.add("uncles", unclesArray);
            resp.add("result", result);
            return resp.toString();
        } else if (method.equalsIgnoreCase("eth_getTransactionReceipt")) {
            JsonArray array = root.get("params").getAsJsonArray();
            String txHash = array.get(0).getAsString();
            TransactionReceipt receipt = ethService.getTransactionReceipt(txHash);
            if (receipt == null) return null;
            PlainTransactionReceipt preceipt = (PlainTransactionReceipt) receipt;
            JsonObject result = new JsonObject();
            result.addProperty("transactionHash", preceipt.getTransactionHash());
            result.addProperty("transactionIndex", preceipt.getTransactionIndexString());
            result.addProperty("blockNumber", new BigInteger(preceipt.getBlockNumberString()).toString(16));
            result.addProperty("cumulativeGasUsed", preceipt.getCumulativeGasUsedString());
            result.addProperty("gasUsed", preceipt.getGasUsedString());
            result.addProperty("contractAddress", preceipt.getContractAddress());
            result.addProperty("logsBloom", preceipt.getLogsBloom());
            result.addProperty("status", "0x1");
            resp.add("result", result);
            return resp.toString();
        } else if (method.equalsIgnoreCase("eth_call")) {
            JsonArray array = root.get("params").getAsJsonArray();
            JsonObject tx = array.get(0).getAsJsonObject();
            String to = tx.get("to").getAsString();
            String data = tx.get("data").getAsString();
            org.web3j.protocol.core.methods.request.Transaction transaction =
                    new org.web3j.protocol.core.methods.request.Transaction(null, null, null, null, to, null, data);
            String quantity = array.get(1).getAsString();
            DefaultBlockParameter param = DefaultBlockParameterName.fromString(quantity);
            String value = ethService.call(transaction, param);
            resp.addProperty("result", value);
            return resp.toString();
        }
        throw new BlockchainException("unsupported method: " + method);
    }

    @GetMapping("/eth/transactionByHash")
    public String transactionByHash(@RequestParam("txId") String txHash) throws BlockchainException {
        Transaction transaction = ethService.getTransaction(txHash);
        JsonObject jo = new JsonObject();
        jo.addProperty("value", transaction.getValue());
        jo.addProperty("txId", transaction.getHash());
        jo.addProperty("input", transaction.getInput());
        jo.addProperty("from", transaction.getFrom());
        jo.addProperty("to", transaction.getTo());
        return jo.toString();
    }

    /**
     * Only for debug
     * @param txHash
     * @return
     */
    @GetMapping("/eth/debug_erc20")
    public String debugErc20(@RequestParam("txId") String txHash) {
        return ethService.debugErc20Tx(txHash);
    }

    /**
     * Only for debug
     * @param txHash
     * @return
     */
    @GetMapping("/eth/debug_eth")
    public String debugEth(@RequestParam("txId") String txHash) {
        return ethService.debugEthTx(txHash);
    }
}
