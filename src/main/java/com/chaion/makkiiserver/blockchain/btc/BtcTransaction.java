package com.chaion.makkiiserver.blockchain.btc;

import com.chaion.makkiiserver.SafeGson;
import com.chaion.makkiiserver.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
        String txid = SafeGson.getJsonStringValue(root, "txid");
        tx.setTxId(txid);
        int version = SafeGson.getJsonIntValue(root, "version");
        tx.setVersion(version);
        long locktime = SafeGson.getJsonLongValue(root, "locktime");
        tx.setLocktime(locktime);
        String blockhash = SafeGson.getJsonStringValue(root, "blockhash");
        tx.setBlockHash(blockhash);
        BigInteger blockheight = SafeGson.getJsonBigIntegerValue(root, "blockheight");
        tx.setBlockHeight(blockheight);
        BigInteger confirmations = SafeGson.getJsonBigIntegerValue(root, "confirmations");
        tx.setConfirmations(confirmations);
        long time = SafeGson.getJsonLongValue(root, "time");
        tx.setTime(time);
        long blocktime = SafeGson.getJsonLongValue(root, "blocktime");
        tx.setBlocktime(blocktime);
        BigDecimal valueOut = SafeGson.getJsonBigDecimalValue(root, "valueOut");
        tx.setValueOut(valueOut);
        int size = SafeGson.getJsonIntValue(root, "size");
        tx.setSize(size);
        BigDecimal valueIn = SafeGson.getJsonBigDecimalValue(root, "valueIn");
        tx.setValueIn(valueIn);
        BigDecimal fees = SafeGson.getJsonBigDecimalValue(root, "fees");
        tx.setFees(fees);
        List<BtcTxVin> vins = new ArrayList<>();
        tx.setVin(vins);
        if (root.has("vin")) {
            JsonElement jeVin = root.get("vin");
            if (jeVin.isJsonArray()) {
                JsonArray vinArray = jeVin.getAsJsonArray();
                for (int i = 0; i < vinArray.size(); i++) {
                    if (vinArray.get(i).isJsonObject()) {
                        if (vinArray.get(i).isJsonObject()) {
                            JsonObject jsIn = vinArray.get(i).getAsJsonObject();
                            BtcTxVin vin = new BtcTxVin();
                            vin.setTxId(SafeGson.getJsonStringValue(jsIn, "txid"));
                            vin.setVout(SafeGson.getJsonLongValue(jsIn, "vout"));
                            vin.setSequence(SafeGson.getJsonLongValue(jsIn, "sequence"));
                            vin.setN(SafeGson.getJsonIntValue(jsIn, "n"));
                            vin.setAddr(SafeGson.getJsonStringValue(jsIn, "addr"));
                            vin.setValueSat(SafeGson.getJsonBigIntegerValue(jsIn, "valueSat"));
                            vin.setValue(SafeGson.getJsonBigDecimalValue(jsIn, "value"));
                            vin.setDoubleSpentTxId(SafeGson.getJsonStringValue(jsIn, "doubleSpentTxID"));
                            if (jsIn.has("scriptSig")) {
                                if (jsIn.get("scriptSig").isJsonObject()) {
                                    JsonObject joScriptSig = jsIn.get("scriptSig").getAsJsonObject();
                                    BtcTxScriptSig scriptSig = new BtcTxScriptSig();
                                    scriptSig.setHex(joScriptSig.get("hex").getAsString());
                                    scriptSig.setAsm(joScriptSig.get("asm").getAsString());
                                    vin.setScriptSig(scriptSig);
                                }
                            }
                            vins.add(vin);
                        }
                    }
                }
            }
        }
        if (root.has("vout")) {
            JsonElement jeVout = root.get("vout");
            if (jeVout.isJsonArray()) {
                List<BtcTxVout> vouts = new ArrayList<>();
                tx.setVout(vouts);
                JsonArray voutArray = jeVout.getAsJsonArray();
                for (int i = 0; i < voutArray.size(); i++) {
                    if (voutArray.get(i).isJsonObject()) {
                        JsonObject jsOut = voutArray.get(i).getAsJsonObject();
                        BtcTxVout vout = new BtcTxVout();
                        vout.setValue(SafeGson.getJsonStringValue(jsOut, "value"));
                        vout.setN(SafeGson.getJsonIntValue(jsOut, "n"));
                        vout.setSpentTxId(SafeGson.getJsonStringValue(jsOut, "spentTxId"));
                        vout.setSpentIndex(SafeGson.getJsonBigIntegerValue(jsOut, "spentIndex"));
                        vout.setSpentHeight(SafeGson.getJsonBigIntegerValue(jsOut, "spentHeight"));
                        if (jsOut.has("scriptPubKey")) {
                            if (jsOut.get("scriptPubKey").isJsonObject()) {
                                JsonObject joScriptPubKey = jsOut.get("scriptPubKey").getAsJsonObject();
                                BtcTxScriptPubKey scriptPubKey = new BtcTxScriptPubKey();
                                scriptPubKey.setHex(SafeGson.getJsonStringValue(joScriptPubKey, "hex"));
                                scriptPubKey.setAsm(SafeGson.getJsonStringValue(joScriptPubKey, "asm"));
                                scriptPubKey.setType(SafeGson.getJsonStringValue(joScriptPubKey, "type"));
                                if (joScriptPubKey.has("addresses")) {
                                    if (joScriptPubKey.get("addresses").isJsonArray()) {
                                        JsonArray addressArray = joScriptPubKey.get("addresses").getAsJsonArray();
                                        List<String> addresses = new ArrayList<>();
                                        scriptPubKey.setAddresses(addresses);
                                        for (int j = 0; j < addressArray.size(); j++) {
                                            if (addressArray.get(j).isJsonPrimitive()) {
                                                addresses.add(addressArray.get(j).getAsString());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        vouts.add(vout);
                    }
                }
            }
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