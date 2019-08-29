package com.chaion.makkiiserver.modules.eventlog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(value="Event Log APIs",
        description = "event log records user app behaviors, such as login, register, transfer, etc, etc.")
@RestController
@RequestMapping("eventlog")
public class EventLogController {

    @Autowired
    EventLogRepository eventlogRepo;

    @ApiOperation(value="Add an event log")
    @PutMapping
    public EventLog addEventLog(@RequestBody EventLog eventLog) {
        if (eventLog.getCreated() == null) {
            eventLog.setCreated(System.currentTimeMillis());
        }
        return eventlogRepo.insert(eventLog);
    }
}
