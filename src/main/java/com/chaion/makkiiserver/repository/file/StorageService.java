package com.chaion.makkiiserver.repository.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;

public interface StorageService {
    Path store(MultipartFile file) throws StorageException;
    Path store(MultipartFile file, String targetFilename) throws StorageException;
    Path store(File file) throws StorageException;
    void delete(Path path) throws StorageException;
    Resource loadAsResource(String filename) throws StorageException;
}
