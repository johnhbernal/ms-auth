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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the full Spring Security filter chain and auth endpoints.
 *
 * <p>Spins up the complete application context with H2 in-memory DB.
 * {@code @ActiveProfiles("dev")} activates DataInitializer (seeds admin/user/reader).
 * {@code @MockBean PracticaServiceClient} prevents Feign from calling ms-practica.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerIntegrationTest {

    private static final String LOGIN_URL     = "/api/auth/login";
    private static final String RENEW_URL     = "/api/auth/renew";
    private static final String VALIDATE_URL  = "/api/auth/validate";
    private static final String PROTECTED_URL = "/api/protected";

    @Autowired MockMvc       mockMvc;
    @Autowired ObjectMapper  objectMapper;
    @MockBean  PracticaServiceClient practicaServiceClient;

    private String sessionToken;

    @BeforeAll
    void login_andCaptureToken() throws Exception {
        String body = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"Admin123!\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        sessionToken = objectMapper.readTree(body)
                .path("data").path("sessionToken").asText();
        assertThat(sessionToken).isNotBlank();
    }

    // ── Login ────────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200AndSessionToken() throws Exception {
        String body = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"User123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.sessionToken").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(body).path("data").path("sessionToken").asText();
        assertThat(token).isNotBlank();
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong!!\"}"))  // >= 6 chars, wrong value
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"));
    }

    @Test
    void login_blankUsername_returns400() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"Admin123!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"));
    }

    @Test
    void login_blankPassword_returns400() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"));
    }

    // ── Renew ────────────────────────────────────────────────────────────────

    @Test
    void renewToken_validToken_returns200AndNewToken() throws Exception {
        String body = mockMvc.perform(post(RENEW_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionToken\":\"" + sessionToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.sessionToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String newToken = objectMapper.readTree(body).path("data").path("sessionToken").asText();
        assertThat(newToken).isNotBlank().isNotEqualTo(sessionToken);
    }

    @Test
    void renewToken_invalidToken_returns401() throws Exception {
        mockMvc.perform(post(RENEW_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionToken\":\"not.a.real.token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"));
    }

    // ── Validate ─────────────────────────────────────────────────────────────

    @Test
    void validateToken_validToken_returns200True() throws Exception {
        mockMvc.perform(get(VALIDATE_URL).param("token", sessionToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void validateToken_invalidToken_returns401() throws Exception {
        mockMvc.perform(get(VALIDATE_URL).param("token", "tampered.invalid.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"));
    }

    // ── JWT filter chain ─────────────────────────────────────────────────────

    @Test
    void protectedEndpoint_noToken_returns401JsonContract() throws Exception {
        mockMvc.perform(get(PROTECTED_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.description").value("Unauthorized"));
    }

    @Test
    void protectedEndpoint_validToken_isNotUnauthorized() throws Exception {
        MvcResult result = mockMvc.perform(get(PROTECTED_URL)
                        .header("Authorization", "Bearer " + sessionToken))
                .andReturn();
        assertThat(result.getResponse().getStatus())
                .isNotEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void protectedEndpoint_tamperedToken_returns401() throws Exception {
        mockMvc.perform(get(PROTECTED_URL)
                        .header("Authorization", "Bearer " + sessionToken + "tampered"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"));
    }
}
