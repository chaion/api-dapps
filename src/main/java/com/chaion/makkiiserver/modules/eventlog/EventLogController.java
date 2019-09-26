package com.chaion.makkiiserver.modules.eventlog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Api(value="Event Log APIs",
        description = "event log records user app behaviors, such as login, register, transfer, etc.")
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

    @ApiOperation(value = "get event logs")
    @GetMapping
    public Page<EventLog> getEventLogs(@RequestParam(value = "offset") int offset,
                                       @RequestParam(value = "size") int limit,
                                       @RequestParam(value = "event", required = false) String event,
                                       @RequestParam(value = "startDate", required = false) Long startDate,
                                       @RequestParam(value = "endDate", required = false) Long endDate) {
        if (startDate == null) startDate = 0L;
        if (endDate == null) endDate = Long.MAX_VALUE;
        Pageable page = PageRequest.of(offset, limit);
        if (event != null) {
            return eventlogRepo.findEventsByEventInAndCreatedBetween(Arrays.asList(event.split(",")),
                    startDate, endDate, page);
        } else {
            return eventlogRepo.findEventsByCreatedBetween(startDate, endDate, page);
        }

    }
}
