package co.com.practica.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that mirrors the {@code ParametroDTO} structure of ms-practica.
 * Used in Feign calls to create or update parameters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterFeignDto {

    private Integer parameterCode;
    private String  parameterName;
    private String  parameterValue;
    private String  parameterDescription;
    private String  status;
}
