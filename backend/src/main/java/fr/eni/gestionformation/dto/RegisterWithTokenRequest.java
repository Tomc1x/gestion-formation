package fr.eni.gestionformation.dto;

import lombok.Data;

@Data
public class RegisterWithTokenRequest {
    private String token;
    private String password;
    private String firstName;
    private String lastName;
}
