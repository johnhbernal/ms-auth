package co.com.practica.auth.dto.directory;

import co.com.practica.auth.entity.User;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/** Result of {@link co.com.practica.auth.service.SimulatedDirectoryService#bind}. */
@Value
@Builder
public class DirectoryBindResult {

    User user;
    String distinguishedName;
    List<String> memberOf;
}
