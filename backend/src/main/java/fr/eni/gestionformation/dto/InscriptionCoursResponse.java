package fr.eni.gestionformation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class InscriptionCoursResponse {
    private Long id;
    private Long eleveId;
    private Long coursPlanifieId;
    private LocalDate dateInscription;
    private List<String> warnings;
}
