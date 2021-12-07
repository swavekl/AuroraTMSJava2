package com.auroratms.utils.filerepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileRepositoryFactory {

    @Value("${deploymentType}")
    private String deploymentType;

    @Autowired
    private LocalFileRepository localFileRepository;

    public IFileRepository getFileRepository() {
        if (deploymentType.equals("local")) {
            return localFileRepository;
        } else {
            throw new RuntimeException("Can't instantiate file repository for deployment of type " + deploymentType);
        }
    }

}
