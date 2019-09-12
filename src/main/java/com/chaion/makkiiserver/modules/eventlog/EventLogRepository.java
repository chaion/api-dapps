package com.chaion.makkiiserver.modules.eventlog;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventLogRepository extends MongoRepository<EventLog, String> {

    @Query("{event: '?0', created: {$gt: ?1, $lt: ?2}}")
    List<EventLog> findEventsByTimeRange(String event, Long startTime, Long endTime);

}
