package co.com.practica.auth.controller;

import co.com.practica.auth.util.PracticaServiceClient;
import com.fasterxml.jackson.databind.JsonNode;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RbacControllerIntegrationTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean PracticaServiceClient practicaServiceClient;

    private String adminToken;
    private String userToken;

    @BeforeAll
    void login() throws Exception {
        adminToken = login("admin", "Admin123!");
        userToken  = login("user", "User123!");
    }

    @Test
    void listPermissions_operatorUser_returns200() throws Exception {
        mockMvc.perform(get("/api/rbac/permissions")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void createGroup_userWithoutGroupAdmin_returns403() throws Exception {
        mockMvc.perform(post("/api/rbac/groups")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"G-Test\",\"description\":\"x\","
                                + "\"distinguishedName\":\"CN=G-Test,OU=Groups,DC=practica,DC=local\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("403"));
    }

    @Test
    void createGroup_admin_returns201() throws Exception {
        mockMvc.perform(post("/api/rbac/groups")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"G-Integration\",\"description\":\"IT\","
                                + "\"distinguishedName\":\"CN=G-Integration,OU=Groups,DC=practica,DC=local\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.data.name").value("G-Integration"));
    }

    @Test
    void directoryMe_admin_returnsGroupsAndPermissions() throws Exception {
        mockMvc.perform(get("/api/directory/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.groups").isArray())
                .andExpect(jsonPath("$.data.permissions").isArray())
                .andExpect(jsonPath("$.data.distinguishedName").isNotEmpty());
    }

    @Test
    void createPermissionAndRole_admin_returns201() throws Exception {
        mockMvc.perform(post("/api/rbac/permissions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"DEMO_REPORT_READ\",\"description\":\"Demo report\","
                                + "\"module\":\"DEMO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("DEMO_REPORT_READ"))
                .andExpect(jsonPath("$.data.module").value("DEMO"));

        mockMvc.perform(post("/api/rbac/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"DEMO_VIEWER\",\"description\":\"Demo\","
                                + "\"permissionCodes\":[\"DEMO_REPORT_READ\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("DEMO_VIEWER"))
                .andExpect(jsonPath("$.data.permissions[0]").value("DEMO_REPORT_READ"));
    }

    @Test
    void inventoryDemo_seller_canReadPricesButNotWrite() throws Exception {
        String sellerToken = login("seller", "Seller123!");

        mockMvc.perform(get("/api/demo/inventario/productos")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(put("/api/demo/inventario/productos/precio")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU-001\",\"price\":1}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/demo/inventario/productos/stock")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU-001\",\"quantity\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void inventoryDemo_anonymous_returns401() throws Exception {
        mockMvc.perform(get("/api/demo/inventario/productos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listPermissions_seededInventario_hasModuleInventario() throws Exception {
        String body = mockMvc.perform(get("/api/rbac/permissions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode arr = objectMapper.readTree(body).path("data");
        boolean found = false;
        for (JsonNode n : arr) {
            if ("INVENTARIO_PRECIO_READ".equals(n.path("code").asText())) {
                assertThat(n.path("module").asText()).isEqualTo("INVENTARIO");
                found = true;
            }
        }
        assertThat(found).as("INVENTARIO_PRECIO_READ must be seeded").isTrue();
    }

    @Test
    void inventoryDemo_admin_canUpdatePriceAndStock() throws Exception {
        mockMvc.perform(put("/api/demo/inventario/productos/precio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU-001\",\"price\":19900}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.price").value(19900));

        mockMvc.perform(put("/api/demo/inventario/productos/stock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU-002\",\"quantity\":50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(50));
    }

    @Test
    void inventoryDemo_admin_invalidPriceBody_returns400() throws Exception {
        mockMvc.perform(put("/api/demo/inventario/productos/precio")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"\",\"price\":-1}"))
                .andExpect(status().isBadRequest());
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return node.path("data").path("sessionToken").asText();
    }
}
