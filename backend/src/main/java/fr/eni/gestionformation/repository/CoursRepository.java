package fr.eni.gestionformation.repository;

import fr.eni.gestionformation.entity.Cours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoursRepository extends JpaRepository<Cours, Long> {
    List<Cours> findByCursusId(Long cursusId);
}
