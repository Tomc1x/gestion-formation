package fr.eni.gestionformation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvitationPreviewResponse {
    private String email;
    private String role;
}
