package fr.eni.gestionformation.dto;

import fr.eni.gestionformation.entity.enums.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;
}
