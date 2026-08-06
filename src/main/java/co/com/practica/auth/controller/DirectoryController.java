package co.com.practica.auth.controller;

import co.com.practica.auth.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/** Simulated Active Directory view for the authenticated caller. */
@Tag(name = "Directory", description = "Simulated AD identity and membership")
public interface DirectoryController {

    @Operation(summary = "Current directory identity", description = "DN, memberOf, roles and permissions")
    ResponseEntity<ApiResponse> me();
}
