package com.chaion.makkiiserver.modules.pokket;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.junit.Test;

import java.net.URL;
import java.util.List;

public class RSSTest {

    @Test
    public void testUsethebitcoin() throws Exception {
        URL feedSource = new URL("https://usethebitcoin.com/feed/");
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedSource));
        List<SyndEntry> list = feed.getEntries();
        for (SyndEntry se : list) {
            System.out.println("-----------------------------------");
            System.out.println(se);
        }
    }

    @Test
    public void testCointelegraph() throws Exception {
        URL feedSource = new URL("https://cointelegraph.com/rss");
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedSource));
        List<SyndEntry> list = feed.getEntries();
        for (SyndEntry se : list) {
            System.out.println("-----------------------------------");
            System.out.println(se);
        }
    }

    @Test
    public void testbitcoin() throws Exception {
        URL feedSource = new URL("https://news.bitcoin.com/feed/");
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedSource));
        List<SyndEntry> list = feed.getEntries();
        for (SyndEntry se : list) {
            System.out.println("-----------------------------------");
            System.out.println(se);
        }
    }

    @Test
    public void testCoinVoice() throws Exception {
        URL feedSource = new URL("http://www.coinvoice.cn/feed");
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedSource));
        List<SyndEntry> list = feed.getEntries();
        for (SyndEntry se : list) {
            System.out.println("-----------------------------------");
            System.out.println(se);
        }

    }
}
