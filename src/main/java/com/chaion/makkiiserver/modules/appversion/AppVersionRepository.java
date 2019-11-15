package com.chaion.makkiiserver.modules.appversion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppVersionRepository extends MongoRepository<AppVersion, String> {
    AppVersion findFirstByPlatformIgnoreCaseOrderByVersionCodeDesc(String platform);

    @Query("{'platform': {$regex: '?0', $options: 'i'}, 'mandatory': ?1, 'versionCode': { $gt: ?2}}")
    List<AppVersion> findMandatoryVersions(String platform, boolean isMandatory, int versionCode);

    Page<AppVersion> findByVersionCodeLessThanEqualAndPlatformInOrderByReleaseDateDesc(
            int maxVersionCode,
            List<String> platforms,
            Pageable page);
}
