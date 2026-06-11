package fr.eni.gestionformation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CursusResponse {
    private Long id;
    private String name;
    private Long filiereId;
    private String filiereName;
    private List<CoursInCursusResponse> cours;
}
