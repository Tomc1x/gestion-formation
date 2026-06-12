package fr.eni.gestionformation.dto;

import lombok.Data;

@Data
public class UserAdminUpdateRequest {
    private String firstName;
    private String lastName;
    private String email;
}
