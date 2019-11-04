package com.chaion.makkiiserver.modules.news;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
public class CoinVoiceItem {
    @Id
    private String id;
    private String title;
    private String link;
    private Long pubDate;
    private String creator;
    private List<String> categories;
    private String descriptionType;
    private String description;
    private String imageLink;
}
