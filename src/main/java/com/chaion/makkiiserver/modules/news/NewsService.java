package com.chaion.makkiiserver.modules.news;

import com.chaion.makkiiserver.modules.config.ConfigService;
import com.chaion.makkiiserver.modules.config.ModuleConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

@Service
public class NewsService {

    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);

    @Autowired
    ConfigService configService;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    NewsRepository repo;

    public Page<NewsItem> getNews(String newsChannel, int offset, int limit) {
        Pageable page = PageRequest.of(offset, limit);
        return repo.findBySourceOrderByPubDateDesc(newsChannel, page);
    }

    public void fetchAll() {
        ModuleConfig newsConfig = configService.getModule("News");
        Map<String, String> params = newsConfig.getModuleParams();
        Collection<String> values = params.values();
        JsonParser jsonParser = new JsonParser();
        for (String value : values) {
            JsonElement je = jsonParser.parse(value);
            if (je.isJsonArray()) {
                JsonArray array = je.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    je = array.get(i);
                    if (je.isJsonObject()) {
                        JsonObject jo = je.getAsJsonObject();
                        if (jo.has("name") && jo.has("feed")) {
                            try {
                                String name = jo.get("name").getAsString();
                                String feed = jo.get("feed").getAsString();
                                fetch(name, feed);
                            } catch (Exception e) {
                                logger.error(e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    public void fetch(String name, String url) throws IOException, FeedException {
        logger.info("fetching {} news from {}...", name, url);

        URL feedSource = new URL(url);
        URLConnection conn = feedSource.openConnection();
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(conn));
        List<SyndEntry> list = feed.getEntries();
        for (SyndEntry entry : list) {
            NewsItem item = NewsItem.fromSyndEntry(entry);
            item.setSource(name);

            if (repo.findById(item.getId()).isEmpty()) {
                repo.save(item);
            }
        }

        logger.info("fetch {} {} news finish.", list.size(), name);
    }

    @Deprecated
    private void extractImage(NewsItem item) {
        String html = restTemplate.getForObject(item.getLink(), String.class);
        Document root = Jsoup.parse(html);
        Elements eles = root.getElementsByAttributeValue("itemprop", "articleBody");
        if (eles.size() > 0) {
            eles = eles.get(0).getElementsByTag("img");
            if (eles.size() > 0) {
                item.setImageLink(eles.get(0).attr("src"));
            }
        }
    }

}
