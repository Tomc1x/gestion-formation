package fr.eni.gestionformation.dto;

import fr.eni.gestionformation.entity.CoursPlanifieStatut;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class CoursPlanifieResponse {
    private Long id;
    private Long coursId;
    private String coursNom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int ordre;
    private CoursPlanifieStatut statut;
    private Long formateurId;
    private String formateurNom;
    private String salle;
    private List<String> warnings;
}
