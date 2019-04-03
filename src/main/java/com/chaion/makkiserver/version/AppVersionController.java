package com.chaion.makkiserver.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
public class AppVersionController {
    private static final Logger logger = LoggerFactory.getLogger(AppVersionController.class);

    @Autowired
    AppVersionRepository repo;

    @RequestMapping(value="/appVersion", method= RequestMethod.PUT)
    public AppVersion addVersion(@RequestParam(value = "version") String version,
                             @RequestParam(value = "versionCode") int versionCode,
                             @RequestParam(value = "platform") String platform,
                             @RequestParam(value = "mandatory") boolean mandatory,
                             @RequestParam(value = "updates") String updates,
                             @RequestParam(value= "url") String url) {
        AppVersion model = new AppVersion();
        model.setVersion(version);
        model.setVersionCode(versionCode);
        model.setMandatory(mandatory);
        model.setPlatform(platform);
        model.setUpdates(updates);
        model.setUrl(url);
        return repo.insert(model);
    }

    @RequestMapping(value="/appVersion", method=RequestMethod.POST)
    public AppVersion updateVersion(@RequestParam(value = "id") String id,
                                    @RequestParam(value = "version") String version,
                                    @RequestParam(value = "versionCode") int versionCode,
                                    @RequestParam(value = "platform") String platform,
                                    @RequestParam(value = "mandatory") boolean mandatory,
                                    @RequestParam(value = "updates") String updates,
                                    @RequestParam(value = "url") String url
                                    ) {
        Optional<AppVersion> optVersion = repo.findById(id);
        if (optVersion.isPresent()) {
            AppVersion appVersion = optVersion.get();
            appVersion.setVersion(version);
            appVersion.setVersionCode(versionCode);
            appVersion.setUpdates(updates);
            appVersion.setPlatform(platform);
            appVersion.setMandatory(mandatory);
            appVersion.setUrl(url);
            return repo.save(appVersion);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Version(id=" + id + ") Not found.");
    }

    @RequestMapping(value="/appVersion/latest", method=RequestMethod.GET)
    public AppVersion getAppVersion(@RequestParam(value = "versionCode") int currentVersionCode,
                                    @RequestParam(value = "platform") String platform) {
        AppVersion appVersion = repo.findFirstByPlatformOrderByVersionCodeDesc(platform);
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

        return appVersion;
    }
}
