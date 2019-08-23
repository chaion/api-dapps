package com.chaion.makkiiserver.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;

import java.util.Map;

@ApiModel(description="Event logs: login/register/recovery/transfer/...")
public class EventLog {

    // predefined event types
    public static final String EVENT_REGISTER = "REGISTER";
    public static final String EVENT_RECOVERY = "RECOVERY";
    public static final String EVENT_LOGIN = "LOGIN";
    public static final String EVENT_TRANSFER = "TRANSFER";
    public static final String EVENT_DEX_EXCHANGE = "DEX_EXCHANGE";
    // predefined event data parameters
    public static final String EVENT_PARAM_PLATFORM = "platform";
    public static final String EVENT_PARAM_VERSION = "version";
    public static final String EVENT_PARAM_VERSION_CODE = "version_code";
    public static final String EVENT_PARAM_COIN = "coin";
    public static final String EVENT_PARAM_TOKEN = "token";
    public static final String EVENT_PARAM_AMOUNT = "amount";
    public static final String EVENT_PARAM_SRC_TOKEN = "src_token";
    public static final String EVENT_PARAM_DST_TOKEN = "dst_token";
    public static final String EVENT_PARAM_SRC_QTY = "src_qty";
    public static final String EVENT_PARAM_DST_QTY = "dst_qty";
    public static final String EVENT_PARAM_WALLET_ID = "wallet_id";

    @Id
    private String id;

    @ApiModelProperty(value="user identifier",
            notes = "there is no user name or email in app, app sends phone unique identifier as user id.",
            allowEmptyValue = true, example="sdm660")
    private String user;

    @ApiModelProperty(value="event type", example="TRANSFER")
    private String event;

    @ApiModelProperty(value="data binded to event",
            notes = "use key/value to pass event related data",
            example = "{ \"coin\" : \"AION\", \"token\" : \"MAK\", \"amount\" : \"20\" }")
    private Map<String, String> data;

    @ApiModelProperty(value="reserved field. May be used in the future, for example page duration")
    private Long duration;

    @ApiModelProperty(value="event happen time", example = "1562142522014")
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

    @Override
    public String toString() {
        return "EventLog{" +
                "id='" + id + '\'' +
                ", user='" + user + '\'' +
                ", event='" + event + '\'' +
                ", data=" + data +
                ", duration=" + duration +
                ", created=" + created +
                '}';
    }
}
