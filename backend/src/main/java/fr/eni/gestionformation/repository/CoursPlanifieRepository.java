package fr.eni.gestionformation.repository;

import fr.eni.gestionformation.entity.CoursPlanifie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CoursPlanifieRepository extends JpaRepository<CoursPlanifie, Long> {

    List<CoursPlanifie> findByPromotionIdOrderByOrdre(Long promotionId);

    List<CoursPlanifie> findByPromotionIsNullOrderByOrdre();

    List<CoursPlanifie> findByCoursId(Long coursId);

    List<CoursPlanifie> findByFormateurUidOrderByDateDebut(Long formateurId);

    @Query("""
            SELECT pc FROM CoursPlanifie pc
            JOIN pc.cours.formateurs f
            WHERE f.id = :formateurId
              AND pc.dateDebut <= :dateFin
              AND pc.dateFin >= :dateDebut
            """)
    List<CoursPlanifie> findOverlappingForFormateur(@Param("formateurId") Long formateurId,
                                                       @Param("dateDebut") LocalDate dateDebut,
                                                       @Param("dateFin") LocalDate dateFin);

    @Query("""
            SELECT pc FROM CoursPlanifie pc
            WHERE pc.formateur.uid = :formateurId
              AND pc.dateDebut <= :dateFin
              AND pc.dateFin >= :dateDebut
            """)
    List<CoursPlanifie> findOverlappingForFormateurAssigne(@Param("formateurId") Long formateurId,
                                                             @Param("dateDebut") LocalDate dateDebut,
                                                             @Param("dateFin") LocalDate dateFin);
}
