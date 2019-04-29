package com.chaion.makkiserver.dapps;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DAppRepository extends MongoRepository<DApp, String> {
    @Query(value="{'category': ?0}", fields="{name: 1, packUrl:1, category: 1, _id: 1, tagline:1, logoUrl: 1, platform: 1}")
    List<DApp> findByCategory(Category category, Pageable pageable);

    @Query("{'$or': [ {'name': {$regex: '?0'}}, {'fullDescription': {$regex: '?0'}}, {'tagline': {$regex: '?0'}}]}")
    List<DApp> findByKeyword(String keyword, Pageable pageable);
}
