package fr.eni.gestionformation.repository;

import fr.eni.gestionformation.entity.Cursus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CursusRepository extends JpaRepository<Cursus, Long> {
    List<Cursus> findByFiliereId(Long filiereId);
    Optional<Cursus> findByName(String name);
}
