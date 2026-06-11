package fr.eni.gestionformation.repository;

import fr.eni.gestionformation.entity.InscriptionCours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InscriptionCoursRepository extends JpaRepository<InscriptionCours, Long> {

    List<InscriptionCours> findByEleveUid(Long eleveId);

    List<InscriptionCours> findByCoursPlanifieId(Long coursPlanifieId);

    boolean existsByEleveUidAndCoursPlanifieId(Long eleveId, Long coursPlanifieId);

    Optional<InscriptionCours> findByEleveUidAndCoursPlanifieId(Long eleveId, Long coursPlanifieId);
}
