package com.chaion.makkiserver;

import com.chaion.makkiserver.ethcrawler.EthCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class OnStart implements ApplicationRunner {
    @Autowired
    EthCrawler crawler;

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        crawler.crawl();
    }
}
