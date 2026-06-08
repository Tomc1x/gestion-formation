package fr.eni.gestionformation.dto;

import lombok.Data;

import java.util.List;

@Data
public class CoursRequest {
    private String name;
    private Long cursusId;
    private List<Long> formateurIds;
}
