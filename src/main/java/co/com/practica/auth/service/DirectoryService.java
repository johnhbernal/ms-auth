package co.com.practica.auth.service;

import co.com.practica.auth.dto.rbac.DirectoryMeDto;

public interface DirectoryService {

    DirectoryMeDto currentUserView(String username);
}
