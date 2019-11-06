package com.chaion.makkiiserver.modules.news;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends MongoRepository<NewsItem, String> {

    Page<NewsItem> findBySourceOrderByPubDateDesc(String source, Pageable page);

}
