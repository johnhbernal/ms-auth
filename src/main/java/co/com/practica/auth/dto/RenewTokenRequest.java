package co.com.practica.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * Request body for the token renewal endpoint.
 *
 * <p>The client sends the current {@code sessionToken}.
 * ms-auth extracts the session UUID from the token claims internally,
 * so the client never needs to decode the JWT manually.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenewTokenRequest {

    @NotBlank(message = "Session token is required")
    private String sessionToken;
}
