package com.chaion.makkiserver.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class AppVersionController {
    private static final Logger logger = LoggerFactory.getLogger(AppVersionController.class);

    @Autowired
    AppVersionRepository repo;

    @RequestMapping(value="/appVersion", method= RequestMethod.PUT)
    public AppVersion addVersion(@RequestBody AppVersion model) {
        return repo.insert(model);
    }

    @RequestMapping(value="/appVersion", method=RequestMethod.POST)
    public AppVersion updateVersion(@RequestBody AppVersion updatedVersion) {
        Optional<AppVersion> optVersion = repo.findById(updatedVersion.getId());
        if (optVersion.isPresent()) {
            AppVersion appVersion = optVersion.get();
            appVersion.setVersion(updatedVersion.getVersion());
            appVersion.setVersionCode(updatedVersion.getVersionCode());
            appVersion.setUpdatesMap(updatedVersion.getUpdatesMap());
            appVersion.setPlatform(updatedVersion.getPlatform());
            appVersion.setMandatory(updatedVersion.isMandatory());
            appVersion.setUrl(updatedVersion.getUrl());
            return repo.save(appVersion);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Version(id=" + updatedVersion.getId() + ") Not found.");
    }

    @RequestMapping(value="/appVersion/latest", method=RequestMethod.GET)
    public AppVersion getAppVersion(@RequestParam(value = "versionCode") int currentVersionCode,
                                    @RequestParam(value = "platform") String platform,
                                    @RequestParam(value = "lang", required = false, defaultValue = "en") String lang) {
        AppVersion appVersion = repo.findFirstByPlatformOrderByVersionCodeDesc(platform);
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
}
