package fr.eni.gestionformation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CoursResponse {
    private Long id;
    private String name;
    private Long cursusId;
    private String cursusName;
    private List<FormateurInfo> formateurs;
}
