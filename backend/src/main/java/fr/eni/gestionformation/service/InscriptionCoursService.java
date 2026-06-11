package fr.eni.gestionformation.service;

import fr.eni.gestionformation.dto.OrigineInscription;
import fr.eni.gestionformation.entity.CoursPlanifie;
import fr.eni.gestionformation.entity.InscriptionCours;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.exception.CoursPlanifieNotFoundException;
import fr.eni.gestionformation.exception.InscriptionAlreadyExistsException;
import fr.eni.gestionformation.exception.InscriptionNotFoundException;
import fr.eni.gestionformation.exception.UserNotFoundException;
import fr.eni.gestionformation.repository.CoursPlanifieRepository;
import fr.eni.gestionformation.repository.InscriptionCoursRepository;
import fr.eni.gestionformation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InscriptionCoursService {

    private final InscriptionCoursRepository inscriptionCoursRepository;
    private final CoursPlanifieRepository coursPlanifieRepository;
    private final UserRepository userRepository;

    @Transactional
    public InscriptionCours creerInscription(Long coursPlanifieId, Long eleveId) {
        CoursPlanifie coursPlanifie = coursPlanifieRepository.findById(coursPlanifieId)
                .orElseThrow(() -> new CoursPlanifieNotFoundException(coursPlanifieId));
        User eleve = userRepository.findById(eleveId)
                .orElseThrow(() -> new UserNotFoundException(eleveId));

        boolean dejaCouvertParPromotion = coursPlanifie.getPromotion() != null
                && eleve.getPromotion() != null
                && coursPlanifie.getPromotion().getId().equals(eleve.getPromotion().getId());
        if (dejaCouvertParPromotion) {
            throw new InscriptionAlreadyExistsException(eleveId, coursPlanifieId);
        }

        if (inscriptionCoursRepository.existsByEleveUidAndCoursPlanifieId(eleveId, coursPlanifieId)) {
            throw new InscriptionAlreadyExistsException(eleveId, coursPlanifieId);
        }

        InscriptionCours inscription = InscriptionCours.builder()
                .eleve(eleve)
                .coursPlanifie(coursPlanifie)
                .dateInscription(LocalDate.now())
                .build();
        return inscriptionCoursRepository.save(inscription);
    }

    @Transactional
    public void supprimerInscription(Long coursPlanifieId, Long eleveId) {
        InscriptionCours inscription = inscriptionCoursRepository.findByEleveUidAndCoursPlanifieId(eleveId, coursPlanifieId)
                .orElseThrow(() -> new InscriptionNotFoundException(eleveId, coursPlanifieId));
        inscriptionCoursRepository.delete(inscription);
    }

    /**
     * Liste combinee des eleves inscrits a un CoursPlanifie : eleves de la promotion liee
     * (origine PROMOTION) UNION eleves avec une InscriptionCours individuelle (origine INDIVIDUEL),
     * dedupliques par uid.
     */
    public Map<User, OrigineInscription> getInscritsCombines(Long coursPlanifieId) {
        CoursPlanifie coursPlanifie = coursPlanifieRepository.findById(coursPlanifieId)
                .orElseThrow(() -> new CoursPlanifieNotFoundException(coursPlanifieId));

        Map<User, OrigineInscription> resultat = new LinkedHashMap<>();

        if (coursPlanifie.getPromotion() != null) {
            userRepository.findByPromotionId(coursPlanifie.getPromotion().getId())
                    .forEach(eleve -> resultat.put(eleve, OrigineInscription.PROMOTION));
        }

        List<InscriptionCours> inscriptions = inscriptionCoursRepository.findByCoursPlanifieId(coursPlanifieId);
        inscriptions.forEach(inscription -> resultat.putIfAbsent(inscription.getEleve(), OrigineInscription.INDIVIDUEL));

        return resultat;
    }

    /**
     * Planning agrege d'un eleve : CoursPlanifie de sa promotion (origine PROMOTION) UNION
     * CoursPlanifie ou il a une InscriptionCours individuelle (origine INDIVIDUEL),
     * dedupliques par CoursPlanifie.id.
     */
    public Map<CoursPlanifie, OrigineInscription> getPlanningEleve(Long eleveId) {
        User eleve = userRepository.findById(eleveId)
                .orElseThrow(() -> new UserNotFoundException(eleveId));

        Map<CoursPlanifie, OrigineInscription> resultat = new LinkedHashMap<>();

        if (eleve.getPromotion() != null) {
            coursPlanifieRepository.findByPromotionIdOrderByOrdre(eleve.getPromotion().getId())
                    .forEach(cp -> resultat.put(cp, OrigineInscription.PROMOTION));
        }

        List<InscriptionCours> inscriptions = inscriptionCoursRepository.findByEleveUid(eleveId);
        inscriptions.forEach(inscription -> resultat.putIfAbsent(inscription.getCoursPlanifie(), OrigineInscription.INDIVIDUEL));

        return resultat;
    }
}
