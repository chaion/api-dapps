package com.chaion.makkiiserver.repository;

import com.chaion.makkiiserver.model.AppVersion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppVersionRepository extends MongoRepository<AppVersion, String> {
    AppVersion findFirstByPlatformIgnoreCaseOrderByVersionCodeDesc(String platform);

    @Query("{'platform': ?0, 'mandatory': ?1, 'versionCode': { $gt: ?2}}")
    List<AppVersion> findMandatoryVersions(String platform, boolean isMandatory, int versionCode);
}
