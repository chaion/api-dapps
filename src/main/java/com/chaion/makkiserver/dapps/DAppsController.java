package com.chaion.makkiserver.dapps;

import com.chaion.makkiserver.dapps.verification.DAppPkgProcessor;
import com.chaion.makkiserver.dapps.verification.VerifyException;
import com.chaion.makkiserver.exception.CodedErrorEnum;
import com.chaion.makkiserver.exception.CodedException;
import com.chaion.makkiserver.file.StorageException;
import com.chaion.makkiserver.file.StorageProperties;
import com.chaion.makkiserver.file.StorageService;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class DAppsController {
    private static final Logger logger = LoggerFactory.getLogger(DAppsController.class);

    @Autowired
    DAppRepository repo;

    @Autowired
    StorageService storageService;

    @Autowired
    DAppProperties dappProperties;

    @Autowired
    StorageProperties storageProperties;

    @Autowired
    DAppPkgProcessor pkgProcessor;

    //////////////////////////////////////////////
    // for mobile side
    //////////////////////////////////////////////
    @RequestMapping(value="/dappsByKeyword", method = RequestMethod.GET)
    public List<DApp> getDAppsByKeyword(@RequestParam(value = "keyword") String keyword,
                               @RequestParam(value = "offset") int offset,
                               @RequestParam(value = "limit") int limit) {
        return repo.findByKeyword(keyword, PageRequest.of(offset, limit));
    }

    @GetMapping("/dappsByCategory")
    public List<DApp> getDAppsByCategory(@RequestParam(value = "category") Category category,
                                           @RequestParam(value = "offset") int offset,
                                           @RequestParam(value = "limit") int limit) {
        return repo.findByCategory(category, PageRequest.of(offset, limit));
    }

    @GetMapping("/topDappsByCategories")
    public List<DApp> getTopDAppsByCategories(@RequestParam(value = "categories") String categories,
                                              @RequestParam(value = "limit") int limit) {
        List<DApp> listOfDapp = new ArrayList<>();
        String[] categoriesArray = categories.split(",");
        // validate category list
        Category[] categoryEnumArray = new Category[categoriesArray.length];
        for (int i = 0; i < categoriesArray.length; i++) {
            try {
                categoryEnumArray[i] = Category.valueOf(categoriesArray[i].toUpperCase());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new CodedException(CodedErrorEnum.ERROR_INVAID_PARAM_CATEGORIES, "parameter categories is invalid: " + categories);
            }
        }
        for (Category category : categoryEnumArray) {
            listOfDapp.addAll(repo.findByCategory(category, PageRequest.of(0, limit)));
        }
        return listOfDapp;
    }

    @RequestMapping(value="/dapp", method = RequestMethod.GET)
    public DApp getDapp(@RequestParam(value = "id") String id) {
        Optional<DApp> dapp = repo.findById(id);
        if (dapp.isPresent()) {
            return dapp.get();
        }
        return null;

    }

    //////////////////////////////////////////////
    // for management side
    //////////////////////////////////////////////
    @RequestMapping(value = "/dapps/upload")
    @ResponseBody
    public String upload(@RequestParam(value = "DAppPkg") MultipartFile file) {
        // check if package size is larger than 10M
        logger.info("uploading dapp pkg...");

        long pkgSize = file.getSize();
        logger.info("check package size: " + pkgSize);
        if (pkgSize > dappProperties.getMaxPkgSize()) {
            throw new CodedException(CodedErrorEnum.PKG_SIZE_TOO_LARGE);
        }

        // save zip file where can be download from some url
        Path savedPath = null;
        try {
            savedPath = storageService.store(file);
            logger.info("save dapp pkg to " + savedPath.toString());
        } catch (StorageException e) {
            logger.error("save file failed: " + e.getMessage(), e);
            throw new CodedException(CodedErrorEnum.PKG_STORE_ERROR, e.getMessage());
        }

        // decompress zip file
        ZipFile zipFile = null;
        String destination;
        try {
            zipFile = new ZipFile(savedPath.toFile());
            destination = storageProperties.getTempDir() + File.separator + System.currentTimeMillis() + File.separator;
            logger.info("unzip " + savedPath + " file to " + destination);
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            logger.error("unzip file failed:", e.getMessage());
            deleteFile(savedPath);
            throw new CodedException(CodedErrorEnum.PKG_UNZIP_ERROR, e.getMessage());
        }

        try {
            logger.info("verify dapp package");
            DApp dapp = pkgProcessor.process(new File(destination));
            dapp.setPackUrl(savedPath.toFile().getName());

            return savedPath.toFile().getName();
        } catch (VerifyException e) {
            deleteFile(Paths.get(destination));
            deleteFile(savedPath);

            logger.error("process package failed:", e.getMessage());
            throw e;
        } catch (StorageException e) {
            deleteFile(Paths.get(destination));
            deleteFile(savedPath);

            logger.error("process package failed:", e.getMessage());
            throw new CodedException(CodedErrorEnum.PKG_STORE_ERROR, e.getMessage());
        } catch (Exception e) {
            deleteFile(Paths.get(destination));
            deleteFile(savedPath);

            logger.error("process package failed:", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = null;
        try {
            file = storageService.loadAsResource(filename);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"").body(file);
        } catch (StorageException e) {
            logger.error("load file failed: ", e.getMessage());
            throw new CodedException(CodedErrorEnum.ERROR_FILE_NOT_FOUND);
        }
    }

    private void deleteFile(Path savedPath) {
        try {
            storageService.delete(savedPath);
        } catch (StorageException se) {
            logger.error("delete " + savedPath.toString() + " failed:" + se.getMessage());
        }
    }

}
