package com.auroratms.utils.filerepo;

import com.auroratms.AbstractServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.springframework.test.util.AssertionErrors.assertEquals;

public class LocalFileRepositoryTest extends AbstractServiceTest {

    @Autowired
    private FileRepositoryFactory fileRepositoryFactory;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    public void testCrud () {
        IFileRepository fileRepository = this.fileRepositoryFactory.getFileRepository();
        assertNotNull("file repo factory is null", fileRepository);

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
                    "https://gateway-pc:4200/api/filerepository/download/insurance_request/12/certificate/2020-21+USATT+GL+Certificate+-+Fox+Valley+Park+District.pdf",
                    savedFileUrl);
        } catch (FileRepositoryException e) {
            fail("failed to save");
        }
    }
}
