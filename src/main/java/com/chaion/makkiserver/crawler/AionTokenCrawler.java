package com.chaion.makkiserver.crawler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AionTokenCrawler {
    private static final Logger logger = LoggerFactory.getLogger(AionTokenCrawler.class);

    @Autowired
    RestTemplate rest;
    @Autowired
    AionTokenRepository repository;

    public void crawl() {
        int totalPages = 1;
        int page = 0;
        int count = 0;
        while (page < totalPages) {
            int size = 25;
            String url = "https://mastery-api.aion.network/aion/dashboard/getTokenList?page=" + page + "&size=" + size;
            ResponseEntity<String> resp = rest.getForEntity(url, String.class);
            String body = resp.getBody();
            JsonObject root = new JsonParser().parse(body).getAsJsonObject();
            JsonObject pageJson = root.get("page").getAsJsonObject();
            totalPages = pageJson.get("totalPages").getAsInt();
            int totalElements = pageJson.get("totalElements").getAsInt();
            logger.info("total elements: " + totalElements);


            JsonArray array = root.get("content").getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                ATSToken token = new ATSToken();

                JsonObject tokenJson = array.get(i).getAsJsonObject();
                token.setSymbol(tokenJson.get("symbol").getAsString());
                token.setContractAddr(tokenJson.get("contractAddr").getAsString());
                token.setTotalSupply(tokenJson.get("totalSupply").getAsString());
                token.setTokenDecimal(tokenJson.get("tokenDecimal").getAsString());
                token.setLiquidSupply(tokenJson.get("liquidSupply").getAsString());
                token.setCreatorAddress(tokenJson.get("creatorAddress").getAsString());
                token.setTransactionHash(tokenJson.get("transactionHash").getAsString());
                token.setGranularity(tokenJson.get("granularity").getAsString());
                token.setCreationTimestamp(tokenJson.get("creationTimestamp").getAsString());
                token.setName(tokenJson.get("name").getAsString());

                logger.info("" + token);
                repository.save(token);

                count++;
            }
            stupidWait();
            page++;
        }

        logger.info("total count: " + count);
    }
    private void stupidWait() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }
}
