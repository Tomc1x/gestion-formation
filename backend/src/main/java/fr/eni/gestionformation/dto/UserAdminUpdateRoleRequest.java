package fr.eni.gestionformation.dto;

import fr.eni.gestionformation.entity.enums.Role;
import lombok.Data;

@Data
public class UserAdminUpdateRoleRequest {
    private Role role;
}
