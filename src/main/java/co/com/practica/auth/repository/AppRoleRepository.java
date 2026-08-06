package co.com.practica.auth.repository;

import co.com.practica.auth.entity.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

    Optional<AppRole> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT DISTINCT r FROM AppRole r LEFT JOIN FETCH r.permissions")
    List<AppRole> findAllWithPermissions();
}
