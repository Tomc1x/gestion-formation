package fr.eni.gestionformation.dto;

import lombok.Data;

@Data
public class AddCoursToCursusRequest {
    private Long coursId;
    private Integer ordre;
}
