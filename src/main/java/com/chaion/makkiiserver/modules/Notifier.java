package com.chaion.makkiiserver.modules;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Notifier {

    private static final Logger logger = LoggerFactory.getLogger(Notifier.class);

    @Autowired
    RestTemplate restTemplate;

    public void notify(String msg) {
            try {
                String url = "https://hooks.slack.com/services/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";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity request = new HttpEntity(msg, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
                logger.info("notify " + msg + " response: " + response.toString());
            } catch (Exception e) {
                logger.error("notify " + msg + " failed:" + e.getMessage());
            }
    }

}
