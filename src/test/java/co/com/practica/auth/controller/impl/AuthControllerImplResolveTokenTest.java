package co.com.practica.auth.controller.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerImplResolveTokenTest {

    @Test
    void prefersAuthorizationBearerOverQueryParam() {
        String resolved = AuthControllerImpl.resolveToken(
                "Bearer header-token",
                "query-token");
        assertThat(resolved).isEqualTo("header-token");
    }

    @Test
    void fallsBackToQueryParamWhenHeaderMissing() {
        assertThat(AuthControllerImpl.resolveToken(null, "query-token"))
                .isEqualTo("query-token");
    }

    @Test
    void returnsNullWhenBothMissing() {
        assertThat(AuthControllerImpl.resolveToken(null, null)).isNull();
        assertThat(AuthControllerImpl.resolveToken("Basic x", "  ")).isNull();
    }
}
