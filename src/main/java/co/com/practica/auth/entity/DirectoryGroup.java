package co.com.practica.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Simulated AD security group — maps to {@code memberOf} in directory mode.
 * Groups grant {@link AppRole}s; users inherit roles and permissions transitively.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DIRECTORY_GROUPS")
public class DirectoryGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    /** AD-style DN, e.g. {@code CN=G-Admins,OU=Groups,DC=practica,DC=local}. */
    @Column(name = "DISTINGUISHED_NAME", nullable = false, unique = true)
    private String distinguishedName;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "GROUP_ROLES",
            joinColumns = @JoinColumn(name = "GROUP_ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<AppRole> appRoles = new HashSet<>();

    @ManyToMany(mappedBy = "directoryGroups", fetch = FetchType.LAZY)
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<User> members = new HashSet<>();
}
