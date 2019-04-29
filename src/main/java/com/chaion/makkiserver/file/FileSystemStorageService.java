package com.chaion.makkiserver.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileSystemStorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public Path store(MultipartFile file) throws StorageException {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                throw new StorageException("Cannot store file with relative path outside current directory " + filename);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Path targetPath = this.rootLocation.resolve(filename);
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                logger.info(filename + " was saved to " + targetPath.toAbsolutePath());

                return targetPath;
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public Path store(File file) throws StorageException {
        try {
            if (file.length() <= 0) {
                throw new StorageException("Failed to store empty file " + file.getName());
            }
            Path targetPath = this.rootLocation.resolve(file.getName());
            Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info(file.getName() + " was saved to " + targetPath.toAbsolutePath());
            return targetPath;
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getName(), e);
        }
    }

    @Override
    public void delete(Path path) throws StorageException {
        try {
            if (Files.isDirectory(path)) {
                FileSystemUtils.deleteRecursively(path);
            } else {
                Files.delete(path);
            }
        } catch (IOException e) {
            throw new StorageException("Faield to delete file " + path.getFileName());
        }
    }

    @Override
    public Resource loadAsResource(String filename) throws StorageException {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename, filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, filename, e);
        }
    }
}
