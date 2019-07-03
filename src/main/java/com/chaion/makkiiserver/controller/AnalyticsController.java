package com.chaion.makkiiserver.controller;

import com.chaion.makkiiserver.model.AppPlatform;
import com.chaion.makkiiserver.model.EventLog;
import com.chaion.makkiiserver.model.analytics.TransferInfo;
import com.chaion.makkiiserver.model.analytics.UserInfo;
import com.chaion.makkiiserver.repository.EventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("analytics")
public class AnalyticsController {

    @Autowired
    EventLogRepository eventlogRepo;

    @GetMapping("/register_users")
    public UserInfo getRegisteredUsers(@RequestParam(value="startTime") Long from,
                                   @RequestParam(value="endTime") Long to) {
        Map<String, Long> platformCounts = new HashMap<String, Long>();
        platformCounts.put(AppPlatform.ANDROID.name(), 0L);
        platformCounts.put(AppPlatform.IOS.name(), 0L);

        List<EventLog> list = eventlogRepo.findEventsByTimeRange(EventLog.EVENT_REGISTER, from, to);
        for (EventLog el: list) {
            Map<String, String> data = el.getData();
            String curPlatform = data.get(EventLog.EVENT_PARAM_PLATFORM);
            if (curPlatform != null) {
                for (Map.Entry<String, Long> entry : platformCounts.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(curPlatform)) {
                        platformCounts.put(entry.getKey(), entry.getValue() + 1l);
                        break;
                    }
                }
            }
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setStartTime(from);
        userInfo.setEndTime(to);
        userInfo.setTotal(list.size());
        userInfo.setUserCountsByPlatform(platformCounts);
        return userInfo;
    }

    @GetMapping("/transfers")
    public TransferInfo getTransfers(@RequestParam(value="startTime") Long from,
                                     @RequestParam(value="endTime") Long to) {
        List<EventLog> list = eventlogRepo.findEventsByTimeRange(EventLog.EVENT_TRANSFER, from, to);
        TransferInfo transferInfo = new TransferInfo();
        transferInfo.setStartTime(from);
        transferInfo.setEndTime(to);
        transferInfo.setTotalCount(list.size());

        Map<String, Long> transferCountMap = new HashMap<>();
        transferInfo.setTransferCountMap(transferCountMap);

        Map<String, Double> transferAmountMap = new HashMap<>();
        transferInfo.setTransferAmountMap(transferAmountMap);

        for (EventLog el:  list) {
            Map<String, String> data = el.getData();
            if (data != null) {
                String coin = data.get(EventLog.EVENT_PARAM_COIN);
                String token = data.get(EventLog.EVENT_PARAM_TOKEN);
                Double amount = null;
                try {
                    amount = Double.parseDouble(data.get(EventLog.EVENT_PARAM_AMOUNT));
                } catch (Exception e) {
                    continue;
                }
                if (coin != null && amount != null) {
                    String key = token != null? coin + "/" + token: coin;
                    if (transferCountMap.containsKey(key)) {
                        transferCountMap.put(key, transferCountMap.get(key) + 1);
                    } else {
                        transferCountMap.put(key, 1l);
                    }
                    if (transferAmountMap.containsKey(key)) {
                        transferAmountMap.put(key, transferAmountMap.get(key) + amount);
                    } else {
                        transferAmountMap.put(key, amount);
                    }
                }
            }
        }

        return transferInfo;
    }

}
