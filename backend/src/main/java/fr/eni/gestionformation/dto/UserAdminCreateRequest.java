package fr.eni.gestionformation.dto;

import fr.eni.gestionformation.entity.enums.Role;
import lombok.Data;

@Data
public class UserAdminCreateRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;
}
