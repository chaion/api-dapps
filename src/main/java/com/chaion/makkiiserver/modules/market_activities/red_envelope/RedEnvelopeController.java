package com.chaion.makkiiserver.modules.market_activities.red_envelope;

import com.chaion.makkiiserver.blockchain.BlockchainException;
import com.chaion.makkiiserver.blockchain.aion.AionService;
import com.chaion.makkiiserver.modules.config.ModuleConfig;
import com.chaion.makkiiserver.modules.config.ModuleConfigRepository;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("market_activity/red_envelope")
public class RedEnvelopeController {

    private static final Logger logger = LoggerFactory.getLogger(RedEnvelopeController.class);

    @Autowired
    ModuleConfigRepository moduleRepo;
    @Autowired
    RedEnvelopeHistoryRepo repo;
    @Autowired
    AionService aionService;


    @GetMapping(value = "/image", produces = "application/json; charset=utf-8")
    public String getImage() {
        ModuleConfig config = moduleRepo.findFirstByModuleNameIgnoreCase("RedEnvelope");
        if (config == null) {
            return errorJsonObject("Red Envelope module is not configured.").toString();
        }
        Map<String, String> map = config.getModuleParams();
        if (map == null) {
            return errorJsonObject("Red Envelope's ModuleParams is not configured.").toString();
        }
        if (!map.containsKey("imageUrl")) {
            return errorJsonObject("Red Envelope's imageUrl is not configured.").toString();
        }
        JsonObject o = new JsonObject();
        o.addProperty("imageUrl", map.get("imageUrl"));
        o.addProperty("imageLink", map.get("imageLink"));
        return successJsonObject(o).toString();
    }

    /**
     * validate if phone id and address already received red envelope.
     *
     * @param phoneId
     * @param address
     * @return
     */
    @PostMapping(value = "/validate", produces = "application/json; charset=utf-8")
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
                return errorJsonObject("红包模块未开放").toString();
            } else {
                if (!verifyCode.equalsIgnoreCase(params.get("verifyCode"))) {
                    return errorJsonObject("无效的签到码").toString();
                }
            }
        } else {
            return errorJsonObject("红包模块未开放").toString();
        }

        List<RedEnvelopeHsitoryStatus> in = new ArrayList<>();
        in.add(RedEnvelopeHsitoryStatus.PENDING);
        in.add(RedEnvelopeHsitoryStatus.SUCCESS);
        if (repo.countByPhoneIdAndStatusIn(phoneId, in) != 0) {
            return errorJsonObject("该手机已领取过签到红包").toString();
        }
        if (repo.countByAddressAndStatusIn(address, in) != 0) {
            return errorJsonObject("该地址" + address + " 已领取过签到红包").toString();
        }
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
        int randomRange = 50;
        ModuleConfig config = moduleRepo.findFirstByModuleNameIgnoreCase("RedEnvelope");
        if (config != null && config.isEnabled()) {
            Map<String, String> moduleParams = config.getModuleParams();
            if (moduleParams == null || !moduleParams.containsKey("privateKey")) {
                return errorJsonObject("privateKey is missing in Envelope module.").toString();
            }
            pk = moduleParams.get("privateKey");
            if (moduleParams.containsKey("maxAmount")) {
                randomRange = Integer.parseInt(moduleParams.get("maxAmount"));
            }
        } else {
            return errorJsonObject("RedEnvelope module is disabled.").toString();
        }

        Random random = new Random();
        long amount = random.nextInt(randomRange) + 1;

        // send transaction
        String txHash = null;
        try {
            txHash = aionService.sendTransaction(pk, address, BigInteger.valueOf(amount).multiply(BigInteger.TEN.pow(18)));
            logger.info("add tx {} to pending queue, wait confirmation.", txHash);
            aionService.addPendingTransaction(txHash, ((transactionHash, status) -> {
                RedEnvelopeHistory record = repo.findFirstByTxHash(transactionHash);
                if (record != null) {
                    record.setStatus(status? RedEnvelopeHsitoryStatus.SUCCESS: RedEnvelopeHsitoryStatus.FAIL);
                    repo.save(record);
                    logger.info("{} is updated as {}", transactionHash, status);
                }
            }));
        } catch (BlockchainException e) {
            return errorJsonObject("send transaction fail: " + e.getMessage()).toString();
        }

        RedEnvelopeHistory re = new RedEnvelopeHistory();
        re.setAddress(address);
        re.setPhoneId(phoneId);
        re.setAmount(BigInteger.valueOf(amount));
        re.setTxHash(txHash);
        re.setStatus(RedEnvelopeHsitoryStatus.PENDING);
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
