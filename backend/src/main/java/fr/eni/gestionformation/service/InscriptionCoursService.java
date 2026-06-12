package fr.eni.gestionformation.service;

import fr.eni.gestionformation.dto.OrigineInscription;
import fr.eni.gestionformation.entity.CoursPlanifie;
import fr.eni.gestionformation.entity.CursusCours;
import fr.eni.gestionformation.entity.InscriptionCours;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.exception.CoursPlanifieNotFoundException;
import fr.eni.gestionformation.exception.InscriptionAlreadyExistsException;
import fr.eni.gestionformation.exception.InscriptionHorsOrdreException;
import fr.eni.gestionformation.exception.InscriptionNotFoundException;
import fr.eni.gestionformation.exception.UserNotFoundException;
import fr.eni.gestionformation.repository.CoursPlanifieRepository;
import fr.eni.gestionformation.repository.CursusCoursRepository;
import fr.eni.gestionformation.repository.InscriptionCoursRepository;
import fr.eni.gestionformation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InscriptionCoursService {

    private final InscriptionCoursRepository inscriptionCoursRepository;
    private final CoursPlanifieRepository coursPlanifieRepository;
    private final UserRepository userRepository;
    private final CursusCoursRepository cursusCoursRepository;

    /**
     * Resultat de creerInscription : l'inscription creee et la liste des warnings
     * (non vide uniquement si forcer=true et qu'une regle "souple" a ete contournee).
     */
    public record InscriptionResult(InscriptionCours inscription, List<String> warnings) {
    }

    @Transactional
    public InscriptionResult creerInscription(Long coursPlanifieId, Long eleveId, boolean forcer) {
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

        List<String> warnings = new ArrayList<>();
        if (eleve.getPromotion() != null && eleve.getPromotion().getCursus() != null) {
            List<String> prerequisManquants = calculerPrerequisManquants(eleve, coursPlanifie);
            if (!prerequisManquants.isEmpty()) {
                if (!forcer) {
                    throw new InscriptionHorsOrdreException(eleveId, coursPlanifieId, prerequisManquants);
                }
                warnings.add("Inscription hors-ordre (forcée) : prérequis non couverts = " + prerequisManquants);
            }
        }

        InscriptionCours inscription = InscriptionCours.builder()
                .eleve(eleve)
                .coursPlanifie(coursPlanifie)
                .dateInscription(LocalDate.now())
                .build();
        InscriptionCours saved = inscriptionCoursRepository.save(inscription);
        return new InscriptionResult(saved, warnings);
    }

    /**
     * Calcule la liste des noms de cours du cursus de l'eleve dont l'ordre pedagogique
     * (CursusCours.ordre) est strictement inferieur a celui du cours cible, et pour
     * lesquels l'eleve n'a aucune inscription (PROMOTION ou INDIVIDUEL, tout statut).
     * Retourne une liste vide si le cours cible n'appartient pas au cursus de l'eleve
     * (pas de notion d'ordre applicable dans ce cas).
     */
    private List<String> calculerPrerequisManquants(User eleve, CoursPlanifie coursPlanifieCible) {
        Long cursusId = eleve.getPromotion().getCursus().getId();
        List<CursusCours> cursusCoursOrdonnes = cursusCoursRepository.findByCursusIdOrderByOrdre(cursusId);

        Long coursCibleId = coursPlanifieCible.getCours().getId();
        CursusCours entreeCible = cursusCoursOrdonnes.stream()
                .filter(cc -> cc.getCours().getId().equals(coursCibleId))
                .findFirst()
                .orElse(null);
        if (entreeCible == null) {
            return List.of();
        }
        int ordreCible = entreeCible.getOrdre();

        Map<CoursPlanifie, OrigineInscription> planningEleve = getPlanningEleve(eleve.getUid());

        List<String> manquants = new ArrayList<>();
        for (CursusCours cc : cursusCoursOrdonnes) {
            if (cc.getOrdre() >= ordreCible) {
                continue;
            }
            Long coursId = cc.getCours().getId();
            boolean inscrit = planningEleve.keySet().stream()
                    .anyMatch(cp -> cp.getCours().getId().equals(coursId));
            if (!inscrit) {
                manquants.add(cc.getCours().getName());
            }
        }
        return manquants;
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

    /**
     * Planning d'un formateur : tous les CoursPlanifie pour lesquels il est assigne
     * (CoursPlanifie.formateur == formateurId), tries par date de debut.
     */
    public List<CoursPlanifie> getPlanningFormateur(Long formateurId) {
        if (!userRepository.existsById(formateurId)) {
            throw new UserNotFoundException(formateurId);
        }
        return coursPlanifieRepository.findByFormateurUidOrderByDateDebut(formateurId);
    }
}
