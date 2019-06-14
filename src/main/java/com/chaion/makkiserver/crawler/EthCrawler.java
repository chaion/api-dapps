package com.chaion.makkiserver.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EthCrawler {
    @Autowired
    RestTemplate rest;
    @Autowired
    EthTokenRepository repository;

    public void crawl() {
        int i = 1;
        while (true) {
            int totalPages = fetchTokenListPage(i++);
            if (i > totalPages) break;
        }
    }

    private int fetchTokenListPage(int page) {
        System.out.println("page: " + page);
        stupidWait();
        String html = rest.getForObject("https://etherscan.io/tokens?p=" + page, String.class);
        Document root = Jsoup.parse(html);
        String totalPages = root.selectFirst("#ContentPlaceHolder1_divpagingpanel > nav > ul > li:nth-child(3) > span > strong:nth-child(2)").text();
        Elements tbody = root.getElementsByTag("tbody");
        Elements trs = tbody.get(0).getElementsByTag("tr");
        for (Element tr : trs) {
            EthToken t = new EthToken();

            Elements imgs = tr.getElementsByTag("img");
            t.setImagePath(imgs.get(0).attr("src"));

            Elements as = tr.getElementsByTag("a");
            String link = as.get(0).attr("href");

            String intro = tr.getElementsByClass("media-body").get(0).getElementsByTag("p").text();
            t.setDesc(intro);

            fetchTokenDetailPage(t, link);
        }
        return Integer.parseInt(totalPages);
    }

    private void fetchTokenDetailPage(EthToken t, String link) {
        stupidWait();
        String html = rest.getForObject("https://etherscan.io/" + link, String.class);
        Document root = Jsoup.parse(html);
        String name = root.getElementsByClass("media-body").get(0).getElementsByTag("span").text();
        t.setName(name);
        System.out.println(name);

        Element div = root.selectFirst("#ContentPlaceHolder1_divSummary > div.row.mb-4 > div.col-md-6.mb-3.mb-md-0 > div > div.card-body > div.row.align-items-center > div.col-md-8.font-weight-medium");

        String[] totalSupply = div.text().split(" ");
        t.setTotalSupply(totalSupply[0]);
        t.setSymbol(totalSupply[1]);

        String erc = root.selectFirst("#ContentPlaceHolder1_divSummary > div.row.mb-4 > div.col-md-6.mb-3.mb-md-0 > div > div.card-header.d-flex.justify-content-between.align-items-center > h2 > span").text();
        t.setContractType(erc.replace("[", "").replace("]", ""));

        String contractAddress = root.selectFirst("#ContentPlaceHolder1_divSummary > div.row.mb-4 > div:nth-child(2) > div > div.card-body > div.row.align-items-center > div.col-md-8 > div").getElementsByTag("a").get(0).text();
        t.setContractAddr(contractAddress);

        String decimals = root.selectFirst("#ContentPlaceHolder1_trDecimals > div > div.col-md-8").text();
        t.setTokenDecimal(decimals);

        Element e = root.selectFirst("#ContentPlaceHolder1_tr_officialsite_1 > div > div.col-md-8 > a");
        if (e != null) {
            String site = e.attr("href");
            t.setOfficialSite(site);
        }

        repository.save(t);
    }

    private void stupidWait() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }
}
