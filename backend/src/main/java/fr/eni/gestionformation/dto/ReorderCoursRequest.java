package fr.eni.gestionformation.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReorderCoursRequest {
    private List<Long> coursIds;
}
