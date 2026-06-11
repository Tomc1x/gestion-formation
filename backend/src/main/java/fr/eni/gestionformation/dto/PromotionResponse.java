package fr.eni.gestionformation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class PromotionResponse {
    private Long id;
    private String name;
    private Long cursusId;
    private String cursusNom;
    private LocalDate dateDebut;
    private RythmeResponse rythme;
    private List<EleveInfo> eleves;
    private List<CoursPlanifieResponse> planning;
}
