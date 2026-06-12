package fr.eni.gestionformation.dto;

import fr.eni.gestionformation.entity.CoursPlanifieStatut;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PlanningEleveResponse {
    private Long coursPlanifieId;
    private Long coursId;
    private String coursNom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int ordre;
    private CoursPlanifieStatut statut;
    private OrigineInscription origine;
}
