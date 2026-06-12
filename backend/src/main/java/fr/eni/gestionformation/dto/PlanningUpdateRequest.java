package fr.eni.gestionformation.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PlanningUpdateRequest {
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long formateurId;
    private String salle;
}
