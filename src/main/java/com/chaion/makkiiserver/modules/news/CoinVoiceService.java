package com.chaion.makkiiserver.modules.news;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CoinVoiceService {

    private static final Logger logger = LoggerFactory.getLogger(CoinVoiceService.class);

    private static final String COIN_VOICE_FEED = "http://www.coinvoice.cn/feed";

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    RedisTemplate redisTemplate;

    public void fetch() throws IOException, FeedException {
        URL feedSource = new URL(COIN_VOICE_FEED);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedSource));
        List<SyndEntry> list = feed.getEntries();
        List<CoinVoiceItem> items = new ArrayList<>();
        for (SyndEntry entry : list) {
            CoinVoiceItem item = new CoinVoiceItem();
            List<SyndCategory> syndCategories = entry.getCategories();
            List<String> categories = new ArrayList<>();
            for (SyndCategory c : syndCategories) {
                categories.add(c.getName());
            }
            item.setCategories(categories);
            SyndContent desc = entry.getDescription();
            item.setDescription(desc.getValue());
            item.setDescriptionType(desc.getType());
            item.setCreator(entry.getAuthor());
            item.setTitle(entry.getTitle());
            item.setLink(entry.getLink());
            Date date = entry.getPublishedDate();
            item.setPubDate(date.getTime());

            String html = restTemplate.getForObject(item.getLink(), String.class);
            Document root = Jsoup.parse(html);
            Elements eles = root.getElementsByAttributeValue("itemprop", "articleBody");
            if (eles.size() > 0) {
                eles = eles.get(0).getElementsByTag("img");
                if (eles.size() > 0) {
                    item.setImageLink(eles.get(0).attr("src"));
                }
            }

            items.add(item);
        }
        for (CoinVoiceItem i : items) {
            System.out.println(i);
        }
        redisTemplate.opsForValue().set("coinvoice", items);
        logger.info("fetch " + COIN_VOICE_FEED + " finish.");
    }

}
