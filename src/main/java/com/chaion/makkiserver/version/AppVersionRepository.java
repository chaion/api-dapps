package com.chaion.makkiserver.version;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppVersionRepository extends MongoRepository<AppVersion, String> {
    AppVersion findFirstByPlatformOrderByVersionCodeDesc(String platform);

    @Query("{'platform': ?0, 'mandatory': ?1, 'versionCode': { $gt: ?2}}")
    List<AppVersion> findMandatoryVersions(String platform, boolean isMandatory, int versionCode);
}
