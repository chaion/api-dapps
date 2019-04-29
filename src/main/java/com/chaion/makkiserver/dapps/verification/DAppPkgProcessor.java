package com.chaion.makkiserver.dapps.verification;

import com.chaion.makkiserver.Utils;
import com.chaion.makkiserver.dapps.Category;
import com.chaion.makkiserver.dapps.DApp;
import com.chaion.makkiserver.dapps.DAppRepository;
import com.chaion.makkiserver.dapps.Platform;
import com.chaion.makkiserver.exception.CodedErrorEnum;
import com.chaion.makkiserver.file.StorageException;
import com.chaion.makkiserver.file.StorageService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

@Service
public class DAppPkgProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DAppPkgProcessor.class);

    @Autowired
    StorageService storageService;

    public DApp process(File pkgFile) throws VerifyException, StorageException {
        // verify pkg structure
        File indexHtml = new File(pkgFile, "index.html");
        File manifest = new File(pkgFile, "META-INF/manifest.json");
        File signature = new File(pkgFile, "META-INF/signature.json");
        if (!indexHtml.exists() || !manifest.exists() || !signature.exists()) {
            throw new VerifyException(CodedErrorEnum.PKG_INVALID_STRUCTURE);
        }

        // verify signature
        // TODO:

        // verify manifest fields
        return processManifestFile(pkgFile, manifest);
    }

    private DApp processManifestFile(File rootFile, File file) throws StorageException {
        DApp dapp = new DApp();

        JsonElement root = null;
        try {
            root = new JsonParser().parse(new FileReader(file));
        } catch (FileNotFoundException e) {
            logger.error("parse manifest json failed.", e);
            throw new VerifyException(CodedErrorEnum.PKG_INVALID_MANIFEST);
        }

        if (!root.isJsonObject()) throw new VerifyException(CodedErrorEnum.PKG_INVALID_MANIFEST);
        JsonObject rootJo = root.getAsJsonObject();

        // name
        if (!rootJo.has("name")) throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_NAME_MISSING);
        JsonElement je = rootJo.get("name");
        if (!je.isJsonPrimitive()) throw new VerifyException(CodedErrorEnum.PKG_INVALID_MANIFEST);
        String name = je.getAsString();
        if (name.length() > DApp.NAME_MAX_LENGTH) throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_NAME_LENGTH_INVALID);
        dapp.setName(name);

        // tagline
        if (rootJo.has("tagline")) {
            je = rootJo.get("tagline");
            if (!je.isJsonPrimitive()) throw new VerifyException(CodedErrorEnum.PKG_INVALID_MANIFEST);
            String tagline = je.getAsString();
            if (tagline.length() > DApp.TAGLINE_MAX_LENGTH)
                throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_TAGLINE_LENGTH_INVALID);
            dapp.setTagline(tagline);
        }

        // full description
        if (!rootJo.has("full_description")) throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_FULL_DESCRIPTION_MISSING);
        je = rootJo.get("full_description");
        if (!je.isJsonPrimitive()) throw new VerifyException(CodedErrorEnum.PKG_INVALID_MANIFEST);
        String fullDesc = je.getAsString();
        if (fullDesc.length() > DApp.FULL_DESCRIPTION_MAX_LENGTH) throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_FULL_DESCRIPTION_LENGTH_INVALID);
        dapp.setFullDescription(fullDesc);

        // authors
        if (!rootJo.has("authors")) throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_AUTHOR_MISSING);
        je = rootJo.get("authors");
        if (!je.isJsonPrimitive()) throw new VerifyException(CodedErrorEnum.PKG_INVALID_MANIFEST);
        String authors = je.getAsString();
        dapp.setAuthor(authors);

        // category
        if (!rootJo.has("category")) throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_CATEGORY_MISSING);
        je = rootJo.get("category");
        if (!je.isJsonPrimitive()) throw new VerifyException(CodedErrorEnum.PKG_INVALID_MANIFEST);
        Category category = null;
        try {
            category = Category.valueOf(je.getAsString().toUpperCase());
        } catch (Exception e) {
            throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_CATEGORY_INVALID);
        }
        dapp.setCategory(category);

        // web site url
        if (rootJo.has("website_url")) {
            je = rootJo.get("website_url");
            if (!je.isJsonPrimitive()) throw new VerifyException(CodedErrorEnum.PKG_INVALID_MANIFEST);
            String website_url = je.getAsString();
            if (!UrlValidator.getInstance().isValid(website_url)) {
                throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_WEBSITE_URL_INVALID);
            }
            dapp.setWebsiteUrl(website_url);
        }

        // email
        if (rootJo.has("contact_email")) {
            je = rootJo.get("contact_email");
            if (!je.isJsonPrimitive()) throw new VerifyException(CodedErrorEnum.PKG_INVALID_MANIFEST);
            String contactEmail = je.getAsString();
            if (!EmailValidator.getInstance().isValid(contactEmail)) {
                throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_EMAIL_INVALID);
            }
            dapp.setContactEmail(contactEmail);
        }

        // logo
        if (!rootJo.has("logo")) throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_LOGO_MISSING);
        je = rootJo.get("logo");
        if (!je.isJsonPrimitive()) throw new VerifyException(CodedErrorEnum.PKG_INVALID_MANIFEST);
        File logoFile = new File(rootFile, je.getAsString());
        if (!logoFile.isFile() || !logoFile.exists()) throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_LOGO_FILE_NOT_FOUND);
        int[] imageSize = new int[0];
        try {
            imageSize = Utils.getImageSize(logoFile);
            if (imageSize == null || imageSize.length != 2 || (imageSize[0] != DApp.LOGO_WIDTH || imageSize[1] != DApp.LOGO_HEIGHT)) {
                logger.error("logo image size(" + imageSize[0] + "," + imageSize[1]
                        + ") is invalid, expected is (" + DApp.LOGO_WIDTH + "," + DApp.LOGO_HEIGHT + ")");
                throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_LOGO_SIZE_INVALID);
            }
        } catch (IOException e) {
            logger.error("Failed to get image size of " + logoFile.getAbsolutePath(), e);
            throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_LOGO_SIZE_INVALID);
        }
        dapp.setLogoUrl(storageService.store(logoFile).toFile().getName());

        // advertiseImage
        if (!rootJo.has("advertiseImage")) throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_ADVERTISE_IMAGE_MISSING);
        je = rootJo.get("advertiseImage");
        if (!je.isJsonPrimitive()) throw new VerifyException(CodedErrorEnum.PKG_INVALID_MANIFEST);
        File advertiseImageFile = new File(rootFile, je.getAsString());
        if (!advertiseImageFile.isFile() || !advertiseImageFile.exists()) throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_ADVERTISE_IMAGE_FILE_NOT_FOUND);
        try {
            imageSize = Utils.getImageSize(advertiseImageFile);
            if (imageSize == null || imageSize.length != 2 || (imageSize[0] != DApp.ADVERTISE_IMAGE_WIDTH || imageSize[1] != DApp.ADVERTISE_IMAGE_HEIGHT)) {
                logger.error("advertise image size(" + imageSize[0] + "," + imageSize[1]
                        + ") is invalid, expected is (" + DApp.ADVERTISE_IMAGE_WIDTH+ "," + DApp.ADVERTISE_IMAGE_HEIGHT + ")");
                throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_ADVERTISE_IMAGE_SIZE_INVALID);
            }
        } catch (IOException e) {
            logger.error("Failed to get image size of " + advertiseImageFile.getAbsolutePath(), e);
            throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_ADVERTISE_IMAGE_SIZE_INVALID);
        }
        dapp.setAdvertiseImageUrl(storageService.store(advertiseImageFile).toFile().getName());

        // version
        // platform
        if (!rootJo.has("platform")) throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_PLATFORM_MISSING);
        je = rootJo.get("platform");
        if (!je.isJsonPrimitive()) throw new VerifyException(CodedErrorEnum.PKG_INVALID_MANIFEST);
        Platform platform = null;
        try {
            platform = Platform.valueOf(je.getAsString().toUpperCase());
        } catch (Exception e) {
            throw new VerifyException(CodedErrorEnum.PKG_MANIFEST_PLATFORM_INVALID);
        }
        dapp.setPlatform(platform);

        // license

        return dapp;
    }

}
