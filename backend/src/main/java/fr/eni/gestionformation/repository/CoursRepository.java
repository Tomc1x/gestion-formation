package fr.eni.gestionformation.repository;

import fr.eni.gestionformation.entity.Cours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoursRepository extends JpaRepository<Cours, Long> {

    @Query("SELECT c FROM Cours c JOIN c.prerequis p WHERE p.id = :prerequisId")
    List<Cours> findByPrerequisId(@Param("prerequisId") Long prerequisId);
}
