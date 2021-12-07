package com.auroratms.utils.filerepo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * File repository implementation for local files
 */
@Component
public class LocalFileRepository implements IFileRepository {

    @Value("${local.repository.root.path}")
    private String repositoryRoot;

    public LocalFileRepository() {
    }

    /**
     * Saves the file represented by input stream, in the storage path under the sourceFileName
     *
     * @param inputStream
     * @param sourceFileName
     * @param storagePath
     * @return
     * @throws FileRepositoryException
     */
    @Override
    public String save(InputStream inputStream, String sourceFileName, String storagePath) throws FileRepositoryException {
        String destinationPath = String.format("%s/%s/%s", this.repositoryRoot, storagePath, sourceFileName);
        File toFile = new File(destinationPath);
        File parentDirectory = toFile.getParentFile();
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs();
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(toFile);
            FileCopyUtils.copy(inputStream, fileOutputStream);
            return getStorageURL(storagePath, sourceFileName);
        } catch (IOException e) {
            throw new FileRepositoryException("Unable to copy file to destination", e);
        }
    }

    /**
     * Reads the file located at storage path
     * @param path
     * @return
     * @throws FileRepositoryException
     */
    @Override
    public FileInfo read(String path) throws FileRepositoryException {
        try {
            String repositoryPath = getRepositoryPath(path);
            File file = new File(repositoryPath);
            if (file.exists() && file.isFile()) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.fileInputStream = new FileInputStream(file.getCanonicalPath());
                fileInfo.fileSize = file.length();
                fileInfo.filename = file.getName();
                return fileInfo;
            } else {
                throw new FileRepositoryException("File at path " + path + " was not found", null);
            }
        } catch (Exception e) {
            throw new FileRepositoryException("Unable to get file at path " + path, e);
        }
    }

    /**
     * Makes repository path
     *
     * @param path
     * @return
     */
    private String getRepositoryPath(String path) {
        return String.format("%s/%s", this.repositoryRoot, path);
    }

    /**
     * Gets storage url
     * @param storagePath
     * @param sourceFileName
     * @return
     */
    private String getStorageURL(String storagePath, String sourceFileName) throws UnsupportedEncodingException {
        String encodedSourceFileName = URLEncoder.encode(sourceFileName, "UTF-8");
        return String.format("%s/download?path=%s/%s",
                IFileRepository.REPOSITORY_URL_ROOT, storagePath, encodedSourceFileName);
    }

    /**
     * Extracts path from the url and decodes it from URL encoded version
     * @param url
     * @return
     * @throws UnsupportedEncodingException
     */
    private String extractPath(String url) throws UnsupportedEncodingException {
        String path = url.substring(url.indexOf("path=") + "path=".length());
        return URLDecoder.decode(path, "UTF-8");
    }

    @Override
    public void deleteByURL(String url) throws FileRepositoryException {
        try {
            String path = extractPath(url);
            this.delete(path);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String path) throws FileRepositoryException {
        try {
            String repositoryPath = this.getRepositoryPath(path);
            File file = new File(repositoryPath);
            if (file.exists() && file.isFile()) {
                boolean deleteOk = file.delete();
            }
        } catch (Exception e) {
            throw new FileRepositoryException("Unable to delete file at path " + path, e);
        }
    }

    @Override
    public List<String> list(String path) throws FileRepositoryException {
        List<String> foundFileDownloadUrls = new ArrayList<>();
        String repositoryPath = getRepositoryPath(path);
        File folder = new File (repositoryPath);
        if (folder.exists() && folder.isDirectory()) {
            String[] fileNames = folder.list();
            if (fileNames != null) {
                for (String fileName : fileNames) {
                    try {
                        String fileDownloadUrl = getStorageURL(path, fileName);
                        foundFileDownloadUrls.add(fileDownloadUrl);
                    } catch (UnsupportedEncodingException e) {
                        throw new FileRepositoryException("Unable to form download url for '" + path + "' and filename '" + fileName + "'", e);
                    }
                }
            }
        }
        return foundFileDownloadUrls;
    }
}
