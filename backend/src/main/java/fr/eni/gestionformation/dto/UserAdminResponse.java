package fr.eni.gestionformation.dto;

import fr.eni.gestionformation.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAdminResponse {
    private Long uid;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private boolean enabled;
}
