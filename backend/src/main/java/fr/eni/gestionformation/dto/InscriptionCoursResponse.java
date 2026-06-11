package fr.eni.gestionformation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class InscriptionCoursResponse {
    private Long id;
    private Long eleveId;
    private Long coursPlanifieId;
    private LocalDate dateInscription;
}
