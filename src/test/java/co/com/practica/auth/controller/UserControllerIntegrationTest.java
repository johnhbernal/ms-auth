package co.com.practica.auth.controller;

import co.com.practica.auth.util.PracticaServiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerIntegrationTest {

    private static final String LOGIN_URL = "/api/auth/login";
    private static final String USERS_URL = "/api/users";
    private static final String ME_URL    = "/api/users/me";

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  PracticaServiceClient practicaServiceClient;

    private String adminToken;
    private String userToken;
    private String readerToken;

    @BeforeAll
    void obtainTokens() throws Exception {
        adminToken  = login("admin",  "Admin123!");
        userToken   = login("user",   "User123!");
        readerToken = login("reader", "Read123!");
    }

    // ── GET /api/users ────────────────────────────────────────────────────────

    @Test
    void listUsers_noToken_returns401() throws Exception {
        mockMvc.perform(get(USERS_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"));
    }

    @Test
    void listUsers_userToken_returns403Structured() throws Exception {
        mockMvc.perform(get(USERS_URL).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.description").value("Forbidden"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void listUsers_readerToken_returns403() throws Exception {
        mockMvc.perform(get(USERS_URL).header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("403"));
    }

    @Test
    void listUsers_adminToken_returns200WithUsers() throws Exception {
        mockMvc.perform(get(USERS_URL).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].username").exists())
                .andExpect(jsonPath("$.data[0].passwordHash").doesNotExist());
    }

    // ── GET /api/users/me ─────────────────────────────────────────────────────

    @Test
    void me_noToken_returns401() throws Exception {
        mockMvc.perform(get(ME_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"));
    }

    @Test
    void me_adminToken_returns200WithUsername() throws Exception {
        mockMvc.perform(get(ME_URL).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    void me_userToken_returns200WithUsername() throws Exception {
        mockMvc.perform(get(ME_URL).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.username").value("user"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void me_readerToken_returns200WithUsername() throws Exception {
        mockMvc.perform(get(ME_URL).header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.username").value("reader"))
                .andExpect(jsonPath("$.data.role").value("READONLY"));
    }

    // ── POST /api/users (register) ────────────────────────────────────────────

    @Test
    void register_noToken_returns401() throws Exception {
        mockMvc.perform(post(USERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser1\",\"password\":\"Secret1!\",\"fullName\":\"New\",\"email\":\"new1@test.com\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"));
    }

    @Test
    void register_userToken_returns403() throws Exception {
        mockMvc.perform(post(USERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content("{\"username\":\"newuser2\",\"password\":\"Secret1!\",\"fullName\":\"New\",\"email\":\"new2@test.com\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("403"));
    }

    @Test
    void register_adminToken_validRequest_returns201() throws Exception {
        mockMvc.perform(post(USERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"username\":\"newuser3\",\"password\":\"Secret1!\",\"fullName\":\"New User\",\"email\":\"new3@test.com\",\"role\":\"USER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.data.username").value("newuser3"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    void register_duplicateUsername_returns409() throws Exception {
        mockMvc.perform(post(USERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"username\":\"admin\",\"password\":\"Secret1!\",\"fullName\":\"Dup\",\"email\":\"dup1@test.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("409"))
                .andExpect(jsonPath("$.description").value("Username already exists"));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        mockMvc.perform(post(USERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"username\":\"uniqueuser\",\"password\":\"Secret1!\",\"fullName\":\"Dup\",\"email\":\"admin@practica.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("409"))
                .andExpect(jsonPath("$.description").value("Email already registered"));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("sessionToken").asText();
    }
}
