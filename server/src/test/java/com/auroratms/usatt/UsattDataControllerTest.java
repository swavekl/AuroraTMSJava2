package com.auroratms.usatt;

import com.auroratms.server.ServerApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

//import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


//@RunWith(SpringRunner.class)
//@SpringBootTest(SpringBootTest.WebEnvironment.MOCK, classes = {ServerApplication.class})
//@AutoConfigureMockMvc


@SpringBootTest(classes = {ServerApplication.class})
//@ContextConfiguration
//@TestExecutionListeners(listeners={ServletTestExecutionListener.class,
//        DependencyInjectionTestExecutionListener.class,
//        DirtiesContextTestExecutionListener.class,
//        TransactionalTestExecutionListener.class,
//        WithSecurityContextTestExecutionListener.class})
//@Transactional
//@WebAppConfiguration
//public class UsattDataControllerTest extends AbstractJUnit4SpringContextTests {
//@WebMvcTest(controllers = UsattDataController.class)
public class UsattDataControllerTest {

    @Autowired
    private UsattDataController usattDataController;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "swaveklorenc@yahoo.com", authorities = {"Admins"})
    public void testFetchOneUser () throws Exception {

        String filename = "C:\\myprojects\\DubinaRecords.csv";
        usattDataController.processFile (filename);

        mvc.perform(get("/api/usattplayers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .param("firstName", "Samson")
                .param("lastName", "Dubina")
                .param("page", "0")
                .param("size", "3"))
//                .andExpect(content()
//                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
//                .andExpect(jsonPath("$[0].name", is("bob")));

//                .param("sort", "id,desc") // <-- no space after comma!!!
//                .param("sort", "name,asc")) // <-- no space after comma!!!
//        List<UsattPlayerRecord> playerInfos = usattDataController.listPlayers("Samson", "Dubina", PageRequest.of(1, 3));
//        assertEquals("wrong count", 1, playerInfos.size());
//        UsattPlayerRecord playerInfo = playerInfos.get(0);
//        assertEquals ("wrong first name", playerInfo.getFirstName(), "Samson");
//        assertEquals ("wrong last name", playerInfo.getLastName(), "Dubina");
//        assertEquals ("wrong membership id", playerInfo.getMembershipId().longValue(), 9051L);
//
//        UsattPlayerRecord playerByMembershipId = usattDataController.findPlayerByMembershipId(9051L);
//        assertNotNull("didn't find player", playerByMembershipId);
//        assertEquals ("wrong first name", playerInfo.getFirstName(), "Samson");
//        assertEquals ("wrong last name", playerInfo.getLastName(), "Dubina");
//        assertEquals ("wrong membership id", playerInfo.getMembershipId().longValue(), 9051L);

    }

}
