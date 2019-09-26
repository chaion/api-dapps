package com.chaion.makkiiserver.blockchain.btc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Data
public class BtcTransaction {
    private String txId;
    private int version;
    private long locktime;
    private String blockHash;
    private BigInteger blockHeight;
    private BigInteger confirmations;
    private long blocktime;
    private long time;
    private BigDecimal valueOut;
    private int size;
    private BigDecimal valueIn;
    private BigDecimal fees;
    private List<BtcTxVin> vin;
    private List<BtcTxVout> vout;

    public static BtcTransaction fromJson(JsonObject root) {
        BtcTransaction tx = new BtcTransaction();
        String txid = root.get("txid").getAsString();
        tx.setTxId(txid);
        int version = root.get("version").getAsInt();
        tx.setVersion(version);
        long locktime = root.get("locktime").getAsLong();
        tx.setLocktime(locktime);
        String blockhash = root.get("blockhash").getAsString();
        tx.setBlockHash(blockhash);
        BigInteger blockheight = root.get("blockheight").getAsBigInteger();
        tx.setBlockHeight(blockheight);
        BigInteger confirmations = root.get("confirmations").getAsBigInteger();
        tx.setConfirmations(confirmations);
        long time = root.get("time").getAsLong();
        tx.setTime(time);
        long blocktime = root.get("blocktime").getAsLong();
        tx.setBlocktime(blocktime);
        BigDecimal valueOut = root.get("valueOut").getAsBigDecimal();
        tx.setValueOut(valueOut);
        int size = root.get("size").getAsInt();
        tx.setSize(size);
        BigDecimal valueIn = root.get("valueIn").getAsBigDecimal();
        tx.setValueIn(valueIn);
        BigDecimal fees = root.get("fees").getAsBigDecimal();
        tx.setFees(fees);
        List<BtcTxVin> vins = new ArrayList<>();
        tx.setVin(vins);
        JsonArray vinArray = root.get("vin").getAsJsonArray();
        for (int i = 0; i < vinArray.size(); i++) {
            JsonObject jsIn = vinArray.get(i).getAsJsonObject();
            BtcTxVin vin = new BtcTxVin();
            vin.setTxId(jsIn.get("txid").getAsString());
            vin.setVout(jsIn.get("vout").getAsLong());
            vin.setSequence(jsIn.get("sequence").getAsLong());
            vin.setN(jsIn.get("n").getAsInt());
            vin.setAddr(jsIn.get("addr").getAsString());
            vin.setValueSat(jsIn.get("valueSat").getAsBigInteger());
            vin.setValue(jsIn.get("value").getAsBigDecimal());
            vin.setDoubleSpentTxId(jsIn.get("doubleSpentTxID").getAsString());
            JsonObject joScriptSig = jsIn.get("scriptSig").getAsJsonObject();
            BtcTxScriptSig scriptSig = new BtcTxScriptSig();
            scriptSig.setHex(joScriptSig.get("hex").getAsString());
            scriptSig.setAsm(joScriptSig.get("asm").getAsString());
            vin.setScriptSig(scriptSig);
            vins.add(vin);
        }
        List<BtcTxVout> vouts = new ArrayList<>();
        tx.setVout(vouts);
        JsonArray voutArray = root.get("vout").getAsJsonArray();
        for (int i = 0; i < voutArray.size(); i++) {
            JsonObject jsOut = voutArray.get(i).getAsJsonObject();
            BtcTxVout vout = new BtcTxVout();
            vout.setValue(jsOut.get("value").getAsString());
            vout.setN(jsOut.get("n").getAsInt());
            vout.setSpentTxId(jsOut.get("spentTxId").getAsString());
            vout.setSpentIndex(jsOut.get("spentIndex").getAsBigInteger());
            vout.setSpentHeight(jsOut.get("spentHeight").getAsBigInteger());
            JsonObject joScriptPubKey = jsOut.get("scriptPubKey").getAsJsonObject();
            BtcTxScriptPubKey scriptPubKey = new BtcTxScriptPubKey();
            scriptPubKey.setHex(joScriptPubKey.get("hex").getAsString());
            scriptPubKey.setAsm(joScriptPubKey.get("asm").getAsString());
            scriptPubKey.setType(joScriptPubKey.get("type").getAsString());
            JsonArray addressArray = joScriptPubKey.get("investorAddresses").getAsJsonArray();
            List<String> addresses = new ArrayList<>();
            scriptPubKey.setAddresses(addresses);
            for (int j = 0; j < addressArray.size(); j++) {
                addresses.add(addressArray.get(j).getAsString());
            }
            vouts.add(vout);
        }
        return tx;
    }
}

@Data
class BtcTxVin {
    private String txId;
    private long vout;
    private long sequence;
    private int n;
    private BtcTxScriptSig scriptSig;
    private String addr;
    private BigInteger valueSat;
    private BigDecimal value;
    private String doubleSpentTxId;
}

@Data
class BtcTxScriptSig {
    private String hex;
    private String asm;
}

@Data
class BtcTxVout {
    private String value;
    private int n;
    private BtcTxScriptPubKey scriptPubKey;
    private String spentTxId;
    private BigInteger spentIndex;
    private BigInteger spentHeight;
}

@Data
class BtcTxScriptPubKey {
    private String hex;
    private String asm;
    private List<String> addresses;
    private String type;
}