package fr.eni.gestionformation.repository;

import fr.eni.gestionformation.entity.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FiliereRepository extends JpaRepository<Filiere, Long> {
    Optional<Filiere> findByName(String name);
    Optional<Filiere> findByNameIgnoreCase(String name);
}
