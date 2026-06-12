package fr.eni.gestionformation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InscritResponse {
    private Long eleveId;
    private String firstName;
    private String lastName;
    private OrigineInscription origine;
}
