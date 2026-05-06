package co.com.practica.auth.config;

import co.com.practica.auth.constants.AppConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtAccessDeniedHandlerTest {

    private final JwtAccessDeniedHandler handler = new JwtAccessDeniedHandler();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void handle_sets403Status() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest(), response,
                mock(AccessDeniedException.class));

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void handle_setsJsonContentType() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest(), response,
                mock(AccessDeniedException.class));

        assertThat(response.getContentType()).contains("application/json");
    }

    @Test
    void handle_writesStructuredBody() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest(), response,
                mock(AccessDeniedException.class));

        @SuppressWarnings("unchecked")
        Map<String, Object> body = mapper.readValue(response.getContentAsString(), Map.class);
        assertThat(body.get("code")).isEqualTo(AppConstants.CODE_FORBIDDEN);
        assertThat(body.get("description")).isEqualTo(AppConstants.MSG_FORBIDDEN);
        assertThat(body.get("data")).isNull();
    }
}
