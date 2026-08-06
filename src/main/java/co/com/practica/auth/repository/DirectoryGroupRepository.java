package co.com.practica.auth.repository;

import co.com.practica.auth.entity.DirectoryGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DirectoryGroupRepository extends JpaRepository<DirectoryGroup, Long> {

    Optional<DirectoryGroup> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT DISTINCT g FROM DirectoryGroup g LEFT JOIN FETCH g.appRoles r LEFT JOIN FETCH r.permissions")
    List<DirectoryGroup> findAllWithRolesAndPermissions();

    @Query("SELECT DISTINCT g FROM DirectoryGroup g "
            + "LEFT JOIN FETCH g.appRoles r LEFT JOIN FETCH r.permissions "
            + "LEFT JOIN FETCH g.members WHERE g.id = :id")
    Optional<DirectoryGroup> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT g FROM DirectoryGroup g JOIN g.members u WHERE u.id = :userId")
    List<DirectoryGroup> findAllByMemberUserId(@Param("userId") Long userId);
}
