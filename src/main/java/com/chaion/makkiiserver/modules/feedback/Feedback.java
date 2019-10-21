package com.chaion.makkiiserver.modules.feedback;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

@Data
public class Feedback {
    @Id
    private String id;
    private String feedback;
    private String platform;
    private String osVersion;
    private String phoneModel;
    private String contact;
    private List<String> imageUrls;
    private Date createTime;
}
