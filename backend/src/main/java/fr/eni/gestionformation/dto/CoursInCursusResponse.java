package fr.eni.gestionformation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CoursInCursusResponse {
    private Long id;
    private String name;
    private int ordre;
    private List<FormateurInfo> formateurs;
}
