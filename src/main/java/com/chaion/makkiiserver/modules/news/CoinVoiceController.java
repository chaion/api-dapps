package com.chaion.makkiiserver.modules.news;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("news/coinvoice")
public class CoinVoiceController {

    @Autowired
    CoinVoiceRepository repo;

    @GetMapping
    public Page<CoinVoiceItem> getNews(@RequestParam(value = "offset") int offset,
                                       @RequestParam(value = "size") int limit) {
        Pageable page = PageRequest.of(offset, limit);
        return repo.findAllByOrderByPubDateDesc(page);
    }
}
