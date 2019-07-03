package com.chaion.makkiiserver.model;

import io.swagger.annotations.ApiModel;
import org.springframework.data.annotation.Id;

import java.util.Map;

@ApiModel(description="Event logs: login/transfer/...")
public class EventLog {

    // predefined event types
    public static final String EVENT_REGISTER = "REGISTER";
    public static final String EVENT_PARAM_PLATFORM = "platform";
    public static final String EVENT_LOGIN = "LOGIN";
    public static final String EVENT_TRANSFER = "TRANSFER";
    public static final String EVENT_PARAM_COIN = "coin";
    public static final String EVENT_PARAM_TOKEN = "token";
    public static final String EVENT_PARAM_AMOUNT = "amount";

    @Id
    private String id;

    private String user;

    private String event;

    private Map<String, String> data;

    private Long duration;

    private Long created;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}
