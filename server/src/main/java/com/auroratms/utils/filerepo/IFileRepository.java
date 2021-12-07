package com.auroratms.utils.filerepo;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public interface IFileRepository {

    public static final String REPOSITORY_URL_ROOT = "api/filerepository";

    String save(InputStream inputStream, String originalFilename, String storagePath) throws FileRepositoryException;

    String replace(FileRepositoryItem fileRepositoryItem);

    FileInputStream read (String path);

    void delete (String path) throws FileRepositoryException;

    List<String> list (String path);
}
