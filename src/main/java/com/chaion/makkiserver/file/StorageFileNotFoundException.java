package com.chaion.makkiserver.file;

public class StorageFileNotFoundException extends StorageException {
    private String filename;

    public StorageFileNotFoundException(String message, String filename) {
        super(message);
        this.filename = filename;
    }

    public StorageFileNotFoundException(String message, String filename, Throwable throwable) {
        super(message, throwable);
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
