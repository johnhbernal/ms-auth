package co.com.practica.auth.config;

import co.com.practica.auth.constants.AppConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtAuthEntryPointTest {

    private final JwtAuthEntryPoint entryPoint = new JwtAuthEntryPoint();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void commence_sets401Status() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(new MockHttpServletRequest(), response,
                mock(AuthenticationException.class));

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void commence_setsJsonContentType() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(new MockHttpServletRequest(), response,
                mock(AuthenticationException.class));

        assertThat(response.getContentType()).contains("application/json");
    }

    @Test
    void commence_writesStructuredBody() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(new MockHttpServletRequest(), response,
                mock(AuthenticationException.class));

        @SuppressWarnings("unchecked")
        Map<String, Object> body = mapper.readValue(response.getContentAsString(), Map.class);
        assertThat(body.get("code")).isEqualTo(AppConstants.CODE_UNAUTHORIZED);
        assertThat(body.get("description")).isEqualTo("Unauthorized");
        assertThat(body.get("data")).isNull();
    }
}
