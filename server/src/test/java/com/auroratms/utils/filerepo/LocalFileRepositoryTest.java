package com.auroratms.utils.filerepo;

import com.auroratms.AbstractServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

public class LocalFileRepositoryTest extends AbstractServiceTest {

    @Autowired
    private FileRepositoryFactory fileRepositoryFactory;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    public void testCrud () {
        IFileRepository fileRepository = this.fileRepositoryFactory.getFileRepository();
        assertNotNull(fileRepository, "file repo factory is null");

        FileInputStream fileInputStream = null;
        String sourceFileName = null;
        String storagePath = null;
        try {
            Resource resource = resourceLoader.getResource("filerepofiles/2020-21 USATT GL Certificate - Fox Valley Park District.pdf");
            File file = resource.getFile();
            assertNotNull(file);
            fileInputStream = new FileInputStream(file);
            sourceFileName = file.getName();
            storagePath = "insurance_request/12/certificate";
        } catch (IOException e) {
            fail("failed to load the example document file");
        }
        try {
            String savedFileUrl = fileRepository.save(fileInputStream, sourceFileName, storagePath);
            assertEquals("wrong file url",
                    "api/filerepository/download?path=insurance_request/12/certificate/2020-21+USATT+GL+Certificate+-+Fox+Valley+Park+District.pdf",
                    savedFileUrl);
        } catch (FileRepositoryException e) {
            fail("failed to save");
        }
    }

    @Test
    public void testListing () {
        IFileRepository fileRepository = this.fileRepositoryFactory.getFileRepository();
        assertNotNull(fileRepository, "file repo factory is null");

        FileInputStream fileInputStream = null;
        String sourceFileName = null;
        String storagePath = "tournament/2000/venue_pictures";
        String [] imageFilesResources = {
                "filerepofiles/Natalia_Backhand.jpg",
                "filerepofiles/Polski_Debel.jpg",
                "filerepofiles/Polski_Debel_2.jpg",
        };
        for (String imageFileResource : imageFilesResources) {
            try {
                Resource resource = resourceLoader.getResource(imageFileResource);
                File file = resource.getFile();
                assertNotNull(file);
                fileInputStream = new FileInputStream(file);
                sourceFileName = file.getName();
            } catch (IOException e) {
                fail("failed to load the example image file");
            }
            try {
                String savedFileUrl = fileRepository.save(fileInputStream, sourceFileName, storagePath);
                String filename = imageFileResource.substring("filerepofiles/".length());
                String expectedFileURL = "api/filerepository/download?path=" + storagePath + "/" + filename;
                assertEquals("wrong file url", expectedFileURL, savedFileUrl);
            } catch (FileRepositoryException e) {
                fail("failed to save");
            }
        }

        try {
            List<String> foundImageFileURLs = fileRepository.list(storagePath);
            assertEquals("wrong number of files found", imageFilesResources.length, foundImageFileURLs.size());
            for (String imageFileResource : imageFilesResources) {
                String filename = imageFileResource.substring("filerepofiles/".length());
                String expectedFileURL = "api/filerepository/download?path=" + storagePath + "/" + filename;
                assertTrue(foundImageFileURLs.contains(expectedFileURL), "expected image url not found");
            }
        } catch (FileRepositoryException e) {
            fail("failed to list image files");
        }
    }
}
