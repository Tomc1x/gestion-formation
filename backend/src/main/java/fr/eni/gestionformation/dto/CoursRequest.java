package fr.eni.gestionformation.dto;

import lombok.Data;

import java.util.List;

@Data
public class CoursRequest {
    private String name;
    private List<Long> formateurIds;
    private List<Long> prerequisIds;
}
