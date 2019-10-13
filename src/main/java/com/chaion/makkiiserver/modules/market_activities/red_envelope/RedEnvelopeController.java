package com.chaion.makkiiserver.modules.market_activities.red_envelope;

import com.chaion.makkiiserver.blockchain.BlockchainException;
import com.chaion.makkiiserver.blockchain.aion.AionService;
import com.chaion.makkiiserver.modules.config.ModuleConfig;
import com.chaion.makkiiserver.modules.config.ModuleConfigRepository;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("market_activity/red_envelope")
public class RedEnvelopeController {

    @Autowired
    ModuleConfigRepository moduleRepo;
    @Autowired
    RedEnvelopeHistoryRepo repo;
    @Autowired
    AionService aionService;

    /**
     * validate if phone id and address already received red envelope.
     *
     * @param phoneId
     * @param address
     * @return
     */
    @PostMapping("/validate")
    public String validate(@RequestParam(value="phoneId") String phoneId,
                           @RequestParam(value="address") String address,
                           @RequestParam(value="verifyCode") String verifyCode) {
        if (verifyCode == null || verifyCode.isEmpty()) {
            return errorJsonObject("verifyCode is missing").toString();
        }

        ModuleConfig config = moduleRepo.findFirstByModuleNameIgnoreCase("RedEnvelope");
        if (config != null && config.isEnabled()) {
            Map<String, String> params = config.getModuleParams();
            if (params == null || !params.containsKey("verifyCode")) {
                return errorJsonObject("verifyCode is missing in Envelope module.").toString();
            } else {
                if (!verifyCode.equalsIgnoreCase(params.get("verifyCode"))) {
                    return errorJsonObject("invalid verifyCode").toString();
                }
            }
        } else {
            return errorJsonObject("RedEnvelope module is disabled.").toString();
        }

        if (repo.countByPhoneId(phoneId) != 0) {
            return errorJsonObject(phoneId + " already got red envelope").toString();
        }
        // TODO: uncomment
//        if (repo.countByAddress(address) != 0) {
//            return errorJsonObject(address + " already got red envelope").toString();
//        }
        return successJsonObject(null).toString();
    }

    /**
     * unfold envelope
     *
     * @param address
     * @return
     */
    @PostMapping("/unfold")
    public String unfold(@RequestParam(value = "phoneId") String phoneId,
                         @RequestParam(value = "address") String address) {
        String pk = null;
        ModuleConfig config = moduleRepo.findFirstByModuleNameIgnoreCase("RedEnvelope");
        if (config != null && config.isEnabled()) {
            Map<String, String> moduleParams = config.getModuleParams();
            if (moduleParams == null || !moduleParams.containsKey("privateKey")) {
                return errorJsonObject("privateKey is missing in Envelope module.").toString();
            }
            pk = moduleParams.get("privateKey");
        } else {
            return errorJsonObject("RedEnvelope module is disabled.").toString();
        }

        Random random = new Random();
        long amount = random.nextInt(50);

        // send transaction
        String txHash = null;
        try {
            txHash = aionService.sendTransaction(pk, address, BigInteger.valueOf(amount * 1000000000));
        } catch (BlockchainException e) {
            return errorJsonObject("send transaction fail: " + e.getMessage()).toString();
        }

        RedEnvelopeHistory re = new RedEnvelopeHistory();
        re.setAddress(address);
        re.setPhoneId(phoneId);
        re.setAmount(BigInteger.valueOf(amount));
        re.setTxHash(txHash);
        repo.save(re);

        JsonObject r = new JsonObject();
        r.addProperty("txHash", re.getTxHash());
        r.addProperty("amount", re.getAmount());
        return successJsonObject(r).toString();
    }

    public JsonObject errorJsonObject(String error) {
        JsonObject o = new JsonObject();
        o.addProperty("status", false);
        o.addProperty("error", error);
        return o;
    }

    public JsonObject successJsonObject(JsonObject data) {
        JsonObject o = new JsonObject();
        o.addProperty("status", true);
        if (data != null && !data.isJsonNull()) {
            o.add("data", data);
        }
        return o;
    }
}
