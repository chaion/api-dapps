package com.chaion.makkiiserver.modules.news;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class NewsItem {
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
    private String source;

    public static NewsItem fromSyndEntry(SyndEntry entry) {
        String link = entry.getLink();
        List<SyndCategory> syndCategories = entry.getCategories();
        SyndContent desc = entry.getDescription();
        String author = entry.getAuthor();
        String title = entry.getTitle();
        Date date = entry.getPublishedDate();
        String uri = entry.getUri();

        NewsItem item = new NewsItem();
        item.setId(uri);
        List<String> categories = new ArrayList<>();
        for (SyndCategory c : syndCategories) {
            categories.add(c.getName());
        }
        item.setCategories(categories);
        item.setDescription(desc.getValue());
        item.setDescriptionType(desc.getType());
        item.setCreator(author);
        item.setTitle(title);
        item.setLink(link);
        item.setPubDate(date.getTime());
        return item;
    }
}
