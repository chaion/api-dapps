package com.chaion.makkiserver.crawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AionTokenController {

    @Autowired
    AionTokenRepository repo;

    @GetMapping("/token/aion")
    public List<ATSToken> getTokens(@RequestParam(value = "offset") int offset,
                                    @RequestParam(value = "limit") int limit) {
        Pageable page = PageRequest.of(offset, limit);
        return repo.findAll(page).getContent();
    }

    @GetMapping("/token/aion/search")
    public List<ATSToken> search(@RequestParam(value="keyword") String keyword) {
        if (keyword.matches("^0x[0-9a-fA-F]{64}")) {
            return repo.findByContractAddress(keyword);
        }
        return repo.findByName(keyword);
    }
}
