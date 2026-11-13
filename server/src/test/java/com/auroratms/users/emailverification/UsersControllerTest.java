package com.auroratms.users;

import com.auroratms.profile.UserProfileExtService;
import com.auroratms.server.ServerApplication;
import com.auroratms.utils.EmailService;
import com.auroratms.users.emailverification.EmailVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ServerApplication.class)
@ActiveProfiles("test")
class UsersControllerTest {

    @MockitoBean
    private UserProfileExtService userProfileExtService;
    @MockitoBean
    private EmailService emailService;
    @MockitoBean
    private EmailVerificationService emailVerificationService;
    @MockitoSpyBean
    private UsersController usersController;

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void validVerificationTokenActivatesItsAssociatedStagedUser() throws Exception {
        when(emailVerificationService.hashToken("raw-token")).thenReturn("token-hash");
        when(emailVerificationService.claim("token-hash"))
                .thenReturn(Optional.of(new EmailVerificationService.VerificationClaim("okta-user-id", "token-hash")));
        doReturn("{\"status\":\"STAGED\"}").when(usersController).getOktaUserById("okta-user-id");
        doNothing().when(usersController).activateOktaUserWithoutEmail("okta-user-id");

        mockMvc.perform(post("/api/users/validateEmail")
                        .secure(true)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"raw-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(usersController).activateOktaUserWithoutEmail("okta-user-id");
        verify(emailVerificationService).markUsed("token-hash");
    }

    @Test
    void invalidVerificationTokenDoesNotCallOkta() throws Exception {
        when(emailVerificationService.hashToken("bad-token")).thenReturn("bad-hash");
        when(emailVerificationService.claim("bad-hash")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/validateEmail")
                        .secure(true)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"bad-token\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("INVALID"));

        verify(usersController, never()).getOktaUserById(anyString());
        verify(usersController, never()).getUser(anyString());
        verify(usersController, never()).activateOktaUserWithoutEmail(anyString());
    }

    @Test
    void resendLimitKeepsResponseGenericAndDoesNotSendAnotherEmail() throws Exception {
        when(emailVerificationService.allowSourceRequest(anyString())).thenReturn(true);
        doReturn("{\"id\":\"okta-user-id\",\"status\":\"STAGED\",\"profile\":{\"firstName\":\"Person\"}}")
                .when(usersController).getUser("person@example.com");
        when(emailVerificationService.regenerateForResend("okta-user-id"))
                .thenThrow(new EmailVerificationService.TooManyResendsException());

        mockMvc.perform(post("/api/users/resend-verification")
                        .secure(true)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"person@example.com\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("wait")));

        verify(emailService, never()).sendMessageUsingThymeleafTemplate(anyString(), any(), anyString(), anyString(), anyMap());
    }
}
