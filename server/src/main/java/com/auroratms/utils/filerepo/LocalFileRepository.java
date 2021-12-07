package com.auroratms.utils.filerepo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URLEncoder;
import java.util.List;

@Component
public class LocalFileRepository implements IFileRepository {

    @Value("${local.repository.root.path}")
    private String repositoryRoot;

    public LocalFileRepository() {
    }

    /**
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
            String encodedSourceFilename = URLEncoder.encode(sourceFileName, "UTF-8");
            return String.format("%s/download/%s/%s", IFileRepository.REPOSITORY_URL_ROOT,
                    storagePath, encodedSourceFilename);
        } catch (IOException e) {
            throw new FileRepositoryException("Unable to copy file to destination", e);
        }
    }

    @Override
    public String replace(FileRepositoryItem fileRepositoryItem) {
        return null;
    }

    /**
     *
     * @param path
     * @return
     * @throws FileRepositoryException
     */
    @Override
    public FileInfo read(String path) throws FileRepositoryException {
        try {
            String repositoryPath = String.format("%s/%s", this.repositoryRoot, path);
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

    @Override
    public void delete(String path) throws FileRepositoryException {
        try {
            String repositoryPath = String.format("%s/%s", this.repositoryRoot, path);
            File file = new File(repositoryPath);
            if (file.exists() && file.isFile()) {
                boolean deleteOk = file.delete();
            }
        } catch (Exception e) {
            throw new FileRepositoryException("Unable to delete file at path " + path, e);
        }
    }

    @Override
    public List<String> list(String path) {
        return null;
    }
}
