package com.chaion.makkiiserver.modules.news;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("news")
public class NewsController {

    @Autowired
    NewsService newsService;

    @GetMapping
    public Page<NewsItem> getNews(@RequestParam(value = "newsChannel") String newsChannel,
                                  @RequestParam(value = "offset") int offset,
                                  @RequestParam(value = "size") int limit) {
        return newsService.getNews(newsChannel, offset, limit);
    }
}
