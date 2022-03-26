package com.auroratms.utils.filerepo;

import java.io.FileInputStream;

public class FileInfo {

    String filename;

    long fileSize;

    FileInputStream fileInputStream;

    public String getFilename() {
        return filename;
    }

    public long getFileSize() {
        return fileSize;
    }

    public FileInputStream getFileInputStream() {
        return fileInputStream;
    }
}
