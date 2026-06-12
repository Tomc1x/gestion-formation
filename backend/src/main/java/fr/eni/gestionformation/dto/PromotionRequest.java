package fr.eni.gestionformation.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PromotionRequest {
    private String name;
    private Long cursusId;
    private LocalDate dateDebut;
    private RythmeRequest rythme;
    private List<Long> eleveIds;
}
