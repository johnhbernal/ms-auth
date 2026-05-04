package co.com.practica.auth.util;

import co.com.practica.auth.dto.ApiResponse;
import co.com.practica.auth.dto.ParameterFeignDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign REST client for ms-practica.
 *
 * <p>Feign auto-generates the HTTP implementation from this interface.
 * The base URL is resolved from {@code ms-practica.url} in application properties.
 *
 * <h3>Purpose</h3>
 * <ul>
 *   <li>On login  — stores the master token in the PARAMETERS table of ms-practica.</li>
 *   <li>On renewal — updates that parameter with the new master token.</li>
 * </ul>
 *
 * <p><b>Note:</b> this client uses {@code contextId = "practicaServiceClient"} to
 * avoid bean name conflicts when both Feign clients target the same {@code url}.
 */
@FeignClient(
    name = "ms-practica",
    url  = "${ms-practica.url}/api",
    contextId = "practicaServiceClient"
)
public interface PracticaServiceClient {

    /**
     * Creates a new parameter in ms-practica.
     * Called on first login to register the user's master token.
     *
     * @param bearerToken {@code Authorization: Bearer <token>} header
     * @param parameter   parameter data to persist
     * @return wrapped API response from ms-practica
     */
    @PostMapping("/parametros")
    ApiResponse createParameter(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody ParameterFeignDto parameter
    );

    /**
     * Updates an existing parameter in ms-practica.
     * Called when renewing the master token.
     *
     * @param bearerToken {@code Authorization: Bearer <token>} header
     * @param id          parameter ID to update
     * @param parameter   updated parameter data
     * @return wrapped API response from ms-practica
     */
    @PutMapping("/parametros/{id}")
    ApiResponse updateParameter(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("id") Integer id,
            @RequestBody ParameterFeignDto parameter
    );

    /**
     * Retrieves a parameter by ID from ms-practica.
     * Used to verify the stored master token is still valid.
     *
     * @param bearerToken {@code Authorization: Bearer <token>} header
     * @param id          parameter ID to retrieve
     * @return wrapped API response from ms-practica
     */
    @GetMapping("/parametros/{id}")
    ApiResponse getParameter(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("id") Integer id
    );
}
