package fr.eni.gestionformation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EleveDisponibleInfo {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}
