package com.chaion.makkiiserver.modules.analytics;

import com.chaion.makkiiserver.modules.appversion.AppPlatform;
import com.chaion.makkiiserver.modules.eventlog.EventLog;
import com.chaion.makkiiserver.modules.eventlog.EventLogRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value="App Analytics APIs", description = "App Analytics APIs")
@RestController
@RequestMapping("analytics")
public class AnalyticsController {

    @Autowired
    EventLogRepository eventlogRepo;

    @ApiOperation(value="get registered users statistics in the specified time period")
    @GetMapping("/register_users")
    public UserInfo getRegisteredUsers(
            @RequestParam(value="startTime") Long from,
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

    @ApiOperation(value="get transfer statistics in the specified time period")
    @GetMapping("/transfers")
    public TransferInfo getTransfers(@RequestParam(value="startTime") Long from,
                                     @RequestParam(value="endTime") Long to) {
        List<EventLog> list = eventlogRepo.findEventsByTimeRange(EventLog.EVENT_TRANSFER, from, to);
        TransferInfo transferInfo = new TransferInfo();
        transferInfo.setStartTime(from);
        transferInfo.setEndTime(to);
        transferInfo.setTotalCount(list.size());

        List<CoinTransferInfo> coinTransfers = new ArrayList<>();
        transferInfo.setCoinTransferList(coinTransfers);

        Map<String, CoinTransferInfo> map = new HashMap<>();

        for (EventLog el:  list) {
            Map<String, String> data = el.getData();
            if (data != null) {
                String coin = data.get(EventLog.EVENT_PARAM_COIN);
                String token = data.get(EventLog.EVENT_PARAM_TOKEN);
                Double amount;
                try {
                    amount = Double.parseDouble(data.get(EventLog.EVENT_PARAM_AMOUNT));
                } catch (Exception e) {
                    // ignore exception
                    continue;
                }
                CoinTransferInfo coinTransferInfo;
                if (map.containsKey(coin + token)) {
                    coinTransferInfo = map.get(coin + token);
                    coinTransferInfo.setTotalCount(coinTransferInfo.getTotalCount() + 1);
                    coinTransferInfo.setTotalAmount(coinTransferInfo.getTotalAmount() + amount);
                } else {
                    coinTransferInfo = new CoinTransferInfo();
                    coinTransferInfo.setCoin(coin);
                    coinTransferInfo.setToken(token);
                    coinTransferInfo.setTotalCount(1);
                    coinTransferInfo.setTotalAmount(amount);
                    map.put(coin + token, coinTransferInfo);
                    coinTransfers.add(coinTransferInfo);
                }
            }
        }

        return transferInfo;
    }

}
