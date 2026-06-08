package fr.eni.gestionformation.dto;

import lombok.Data;

@Data
public class UserAdminChangePasswordRequest {
    private String newPassword;
}
