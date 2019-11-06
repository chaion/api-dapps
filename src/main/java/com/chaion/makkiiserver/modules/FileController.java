package com.chaion.makkiiserver.modules;

import com.chaion.makkiiserver.repository.file.StorageException;
import com.chaion.makkiiserver.repository.file.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    StorageService storageService;

    @PostMapping("/image/upload")
    public String upload(@RequestParam(value = "file") MultipartFile file) {
        return saveFile(file);
    }

    private String saveFile(MultipartFile file) {
        String newFilename = UUID.randomUUID().toString() + ".png";
        try {
            String targetPath = "/image/" + newFilename;
            storageService.store(file, newFilename);
            return targetPath;
        } catch (StorageException e) {
            logger.error("failed to save file " + file.getName(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file " + file.getName());
        }
    }

    @PostMapping("/image/uploads")
    public List<String> uploads(@RequestParam("files") MultipartFile[] files) {
        List<String> paths = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                paths.add(saveFile(file));
            }
        }
        return paths;
    }

    @GetMapping(value = "/image/{filename:.+}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        Resource file = null;
        try {
            file = storageService.loadAsResource(filename);
            return ResponseEntity.ok()/*.header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"")*/.body(file);
        } catch (StorageException e) {
            logger.error("load file failed: ", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, filename + " not found");
        }
    }
}
