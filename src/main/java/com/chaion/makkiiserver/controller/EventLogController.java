package com.chaion.makkiiserver.controller;

import com.chaion.makkiiserver.model.EventLog;
import com.chaion.makkiiserver.repository.EventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("eventlog")
public class EventLogController {

    @Autowired
    EventLogRepository eventlogRepo;

    @PutMapping
    public void addEventLog(@RequestBody EventLog eventLog) {
        if (eventLog.getCreated() == null) {
            eventLog.setCreated(System.currentTimeMillis());
        }
        eventlogRepo.insert(eventLog);
    }
}
