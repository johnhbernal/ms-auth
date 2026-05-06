package co.com.practica.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {

    private Long   id;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private String status;
}
