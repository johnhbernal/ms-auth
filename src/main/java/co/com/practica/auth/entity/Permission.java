package co.com.practica.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Fine-grained authorization grant (AuthZ layer), scoped by application module.
 * <p>Convention: {@code MODULE_ACTION} — e.g. {@code INVENTARIO_PRECIO_READ},
 * {@code PARAMETRO_WRITE}. Spring authority = {@code PERM_} + code.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PERMISSIONS")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CODE", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    /** Logical module: INVENTARIO, PARAMETROS, RBAC, CORE, … */
    @Column(name = "MODULE", length = 50)
    @Builder.Default
    private String module = "CORE";
}
