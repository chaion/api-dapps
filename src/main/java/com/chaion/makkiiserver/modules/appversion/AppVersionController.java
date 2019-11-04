package com.chaion.makkiiserver.modules.appversion;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Api(value="Version Management APIs", description="Operations pertaining to app version management")
@RestController
@RequestMapping("appVersion")
public class AppVersionController {
    private static final Logger logger = LoggerFactory.getLogger(AppVersionController.class);

    @Autowired
    AppVersionRepository repo;

    @ApiOperation(value="Add a new app version",
            response=AppVersion.class,
            produces = "application/json")
    @PutMapping
    public AppVersion addVersion(@RequestBody AppVersion version) {
        version.setPlatform(version.getPlatform().toLowerCase());
        return repo.insert(version);
    }

    @ApiOperation(value="Update an existing app version",
            response=AppVersion.class,
            produces = "application/json")
    @PostMapping
    public AppVersion updateVersion(@RequestBody AppVersion version) {
        Optional<AppVersion> optVersion = repo.findById(version.getId());
        if (optVersion.isPresent()) {
            AppVersion appVersion = optVersion.get();
            appVersion.setVersion(version.getVersion());
            appVersion.setVersionCode(version.getVersionCode());
            appVersion.setUpdatesMap(version.getUpdatesMap());
            appVersion.setPlatform(version.getPlatform().toLowerCase());
            appVersion.setMandatory(version.isMandatory());
            appVersion.setUrl(version.getUrl());
            appVersion.setReleaseDate(version.getReleaseDate());
            return repo.save(appVersion);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Version(id=" + version.getId() + ") Not found.");
    }

    @ApiOperation(value="Get the latest app version",
            notes = "The returned version's mandatory is true if any version later than the given one is mandatory.",
            response=AppVersion.class,
            produces = "application/json")
    @GetMapping(value="latest")
    public AppVersion getAppVersion(
            @ApiParam(required = true, value = "current app version code which is an increased integer. " +
                    "versionCode in Android and Build number in iOS", example = "10")
            @RequestParam(value = "versionCode")
                    int currentVersionCode,
            @ApiParam(required = true, value = "current app's platform", allowableValues = "Android, iOS")
            @RequestParam(value = "platform")
                    String platform,
            @ApiParam(value = "in which language update message is", allowableValues = "en, zh", defaultValue = "en")
            @RequestParam(value = "lang", required = false, defaultValue = "en")
                    String lang) {
        logger.info("appVersion/latest: currentVersionCode: " + currentVersionCode + ", platform: " + platform + ", lang: " + lang);
        AppVersion appVersion = repo.findFirstByPlatformIgnoreCaseOrderByVersionCodeDesc(platform);
        if (appVersion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Version for platform(" + platform + ") doesn't exist.");
        }

        logger.info("latest app version for platform " + platform + " is: " + appVersion);
        List<AppVersion> mandatoryVersions = repo.findMandatoryVersions(platform, true, currentVersionCode);
        logger.info("mandatory versions after " + currentVersionCode + " are: ");
        for (AppVersion av : mandatoryVersions) {
            logger.info(av.toString());
        }
        if (mandatoryVersions != null && mandatoryVersions.size() > 0) {
            appVersion.setMandatory(true);
        } else {
            appVersion.setMandatory(false);
        }

        Map<String, String> updatesMap = appVersion.getUpdatesMap();
        for (Map.Entry<String, String> update: updatesMap.entrySet()) {
            if (update.getKey().equals(lang)) {
                Map<String, String> updatesLang = new HashMap<>();
                updatesLang.put(update.getKey(), update.getValue());
                appVersion.setUpdatesMap(updatesLang);
                break;
            }
        }

        return appVersion;
    }

    @ApiOperation(value = "get app version by page")
    @GetMapping
    public Page<AppVersion> getAppVersions(@RequestParam(value = "offset") int offset,
                                           @RequestParam(value = "size") int limit,
                                           @RequestParam(value = "platform", required = false) String platform) {
        List<String> platforms;
        if (platform != null && !platform.isEmpty()) {
            platforms = Arrays.asList(platform.toLowerCase().split(","));
        } else {
            platforms = new ArrayList<>();
            platforms.add("android");
            platforms.add("ios");
        }
        return repo.findByPlatformInOrderByReleaseDateDesc(platforms, PageRequest.of(offset, limit));
    }

    @ApiOperation(value = "delete app version by id")
    @DeleteMapping
    public void deleteAppVersions(@RequestParam(value = "id") String id) {
        repo.deleteById(id);
    }
}
