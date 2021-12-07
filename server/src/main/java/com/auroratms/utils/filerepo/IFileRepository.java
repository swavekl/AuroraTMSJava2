package com.auroratms.utils.filerepo;

import java.io.InputStream;
import java.util.List;

public interface IFileRepository {

    public static final String REPOSITORY_URL_ROOT = "api/filerepository";

    String save(InputStream inputStream, String originalFilename, String storagePath) throws FileRepositoryException;

    FileInfo read (String path) throws FileRepositoryException;

    void deleteByURL (String url) throws FileRepositoryException;

    void delete (String path) throws FileRepositoryException;

    List<String> list (String path) throws FileRepositoryException;
}
