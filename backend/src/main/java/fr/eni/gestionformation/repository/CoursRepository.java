package fr.eni.gestionformation.repository;

import fr.eni.gestionformation.entity.Cours;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursRepository extends JpaRepository<Cours, Long> {
}
