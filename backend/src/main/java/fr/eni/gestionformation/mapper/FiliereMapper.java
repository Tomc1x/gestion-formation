package fr.eni.gestionformation.mapper;

import fr.eni.gestionformation.dto.FiliereResponse;
import fr.eni.gestionformation.entity.Filiere;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FiliereMapper {

    public FiliereResponse toResponseDto(Filiere filiere) {
        return new FiliereResponse(filiere.getId(), filiere.getName());
    }

    public List<FiliereResponse> toResponseDtoList(List<Filiere> filieres) {
        return filieres.stream()
                .map(this::toResponseDto)
                .toList();
    }

    public Filiere toEntity(String name) {
        return Filiere.builder()
                .name(name)
                .build();
    }
}
