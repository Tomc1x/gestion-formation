package fr.eni.gestionformation.dto;

import lombok.Data;

@Data
public class InscriptionCoursRequest {
    private Long eleveId;
    private boolean forcer;
}
