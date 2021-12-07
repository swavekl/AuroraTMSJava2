package com.auroratms.utils.filerepo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URLEncoder;
import java.util.List;

@Component
public class LocalFileRepository implements IFileRepository {

    @Value("${client.host.url}")
    private String clientHostUrl;

    @Value("${local.repository.root.path}")
    private String repositoryRoot;

    public LocalFileRepository() {
    }

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
            return String.format("%s/%s/download/%s/%s", clientHostUrl, IFileRepository.REPOSITORY_URL_ROOT,
                    storagePath, encodedSourceFilename);
        } catch (IOException e) {
            throw new FileRepositoryException("Unable to copy file to destination", e);
        }
    }

    @Override
    public String replace(FileRepositoryItem fileRepositoryItem) {
        return null;
    }

    @Override
    public FileInputStream read(String path) {
        return null;
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
