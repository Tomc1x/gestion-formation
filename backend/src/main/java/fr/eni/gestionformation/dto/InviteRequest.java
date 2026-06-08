package fr.eni.gestionformation.dto;

import fr.eni.gestionformation.entity.enums.Role;
import lombok.Data;

@Data
public class InviteRequest {
    private String email;
    private Role role;
}
