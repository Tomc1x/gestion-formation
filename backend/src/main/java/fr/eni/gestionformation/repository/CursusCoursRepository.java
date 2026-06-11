package fr.eni.gestionformation.repository;

import fr.eni.gestionformation.entity.CursusCours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CursusCoursRepository extends JpaRepository<CursusCours, Long> {
    List<CursusCours> findByCursusIdOrderByOrdre(Long cursusId);
    Optional<CursusCours> findByCursusIdAndCoursId(Long cursusId, Long coursId);
    List<CursusCours> findByCoursId(Long coursId);
}
