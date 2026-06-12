package fr.eni.gestionformation.service;

import fr.eni.gestionformation.dto.PlanningCreateRequest;
import fr.eni.gestionformation.dto.PlanningUpdateRequest;
import fr.eni.gestionformation.dto.PromotionRequest;
import fr.eni.gestionformation.entity.Cours;
import fr.eni.gestionformation.entity.CoursPlanifie;
import fr.eni.gestionformation.entity.CoursPlanifieStatut;
import fr.eni.gestionformation.entity.Cursus;
import fr.eni.gestionformation.entity.Promotion;
import fr.eni.gestionformation.entity.Rythme;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.exception.CoursNotFoundException;
import fr.eni.gestionformation.exception.CoursPlanifieNotFoundException;
import fr.eni.gestionformation.exception.CursusNotFoundException;
import fr.eni.gestionformation.exception.PromotionNotFoundException;
import fr.eni.gestionformation.exception.UserNotFoundException;
import fr.eni.gestionformation.repository.CoursPlanifieRepository;
import fr.eni.gestionformation.repository.CoursRepository;
import fr.eni.gestionformation.repository.CursusRepository;
import fr.eni.gestionformation.repository.InscriptionCoursRepository;
import fr.eni.gestionformation.repository.PromotionRepository;
import fr.eni.gestionformation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final CursusRepository cursusRepository;
    private final CoursPlanifieRepository coursPlanifieRepository;
    private final CoursRepository coursRepository;
    private final InscriptionCoursRepository inscriptionCoursRepository;
    private final PlanificationService planificationService;

    public List<Promotion> findAll() {
        return promotionRepository.findAll();
    }

    public Promotion findById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));
    }

    public Promotion save(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    @Transactional
    public Promotion create(PromotionRequest request) {
        Promotion promotion = new Promotion();
        promotion.setName(request.getName());
        promotion.setDateDebut(request.getDateDebut());
        if (request.getCursusId() != null) {
            promotion.setCursus(findCursus(request.getCursusId()));
        }
        if (request.getRythme() != null) {
            Rythme rythme = Rythme.builder()
                    .semainesCentre(request.getRythme().getSemainesCentre())
                    .semainesEntreprise(request.getRythme().getSemainesEntreprise())
                    .promotion(promotion)
                    .build();
            promotion.setRythme(rythme);
        }

        Promotion saved = promotionRepository.save(promotion);

        if (request.getEleveIds() != null) {
            affecterEleves(saved, request.getEleveIds());
        }

        planificationService.genererPlanning(saved);

        return saved;
    }

    @Transactional
    public Promotion update(Long id, PromotionRequest request) {
        Promotion promotion = findById(id);

        boolean cursusChange = !Objects.equals(
                promotion.getCursus() != null ? promotion.getCursus().getId() : null,
                request.getCursusId());
        boolean dateDebutChange = !Objects.equals(promotion.getDateDebut(), request.getDateDebut());

        promotion.setName(request.getName());
        promotion.setDateDebut(request.getDateDebut());

        if (request.getCursusId() != null) {
            promotion.setCursus(findCursus(request.getCursusId()));
        } else {
            promotion.setCursus(null);
        }

        if (request.getRythme() != null) {
            Rythme rythme = promotion.getRythme();
            if (rythme == null) {
                rythme = Rythme.builder().promotion(promotion).build();
                promotion.setRythme(rythme);
            }
            rythme.setSemainesCentre(request.getRythme().getSemainesCentre());
            rythme.setSemainesEntreprise(request.getRythme().getSemainesEntreprise());
        } else {
            promotion.setRythme(null);
        }

        Promotion saved = promotionRepository.save(promotion);

        if (request.getEleveIds() != null) {
            List<User> ancienEleves = userRepository.findByPromotionId(id);
            List<Long> nouveauxIds = request.getEleveIds();
            List<User> retires = ancienEleves.stream()
                    .filter(eleve -> !nouveauxIds.contains(eleve.getUid()))
                    .toList();
            retires.forEach(eleve -> eleve.setPromotion(null));
            userRepository.saveAll(retires);

            affecterEleves(saved, nouveauxIds);
        }

        if (cursusChange || dateDebutChange) {
            List<CoursPlanifie> ancien = coursPlanifieRepository.findByPromotionIdOrderByOrdre(id);
            coursPlanifieRepository.deleteAll(ancien);
            planificationService.genererPlanning(saved);
        }

        return saved;
    }

    @Transactional
    public void deleteById(Long id) {
        Promotion promotion = findById(id);
        List<User> users = userRepository.findByPromotionId(id);
        users.forEach(user -> user.setPromotion(null));
        userRepository.saveAll(users);
        List<CoursPlanifie> planning = coursPlanifieRepository.findByPromotionIdOrderByOrdre(id);
        deleteInscriptionsForPlanning(planning);
        coursPlanifieRepository.deleteAll(planning);
        promotionRepository.delete(promotion);
    }

    @Transactional
    public void deletePlanning(Long promotionId, Long coursPlanifieId) {
        findById(promotionId);
        CoursPlanifie coursPlanifie = coursPlanifieRepository.findById(coursPlanifieId)
                .orElseThrow(() -> new CoursPlanifieNotFoundException(coursPlanifieId));

        if (coursPlanifie.getPromotion() == null || !coursPlanifie.getPromotion().getId().equals(promotionId)) {
            throw new CoursPlanifieNotFoundException(coursPlanifieId);
        }

        deleteInscriptionsForPlanning(List.of(coursPlanifie));
        coursPlanifieRepository.delete(coursPlanifie);
    }

    private void deleteInscriptionsForPlanning(List<CoursPlanifie> planning) {
        if (planning.isEmpty()) {
            return;
        }
        List<Long> coursPlanifieIds = planning.stream().map(CoursPlanifie::getId).toList();
        inscriptionCoursRepository.deleteAll(inscriptionCoursRepository.findByCoursPlanifieIdIn(coursPlanifieIds));
    }

    @Transactional
    public void clearCursusReferences(Long cursusId) {
        List<Promotion> promotions = promotionRepository.findByCursusId(cursusId);
        promotions.forEach(promotion -> promotion.setCursus(null));
        promotionRepository.saveAll(promotions);
    }

    public List<CoursPlanifie> getPlanning(Long promotionId) {
        return coursPlanifieRepository.findByPromotionIdOrderByOrdre(promotionId);
    }

    public List<User> getEleves(Long promotionId) {
        return userRepository.findByPromotionId(promotionId);
    }

    @Transactional
    public Promotion addEleve(Long promotionId, Long eleveId) {
        Promotion promotion = findById(promotionId);
        User eleve = userRepository.findById(eleveId)
                .orElseThrow(() -> new UserNotFoundException(eleveId));
        eleve.setPromotion(promotion);
        userRepository.save(eleve);
        return promotion;
    }

    @Transactional
    public Promotion removeEleve(Long promotionId, Long eleveId) {
        Promotion promotion = findById(promotionId);
        User eleve = userRepository.findById(eleveId)
                .orElseThrow(() -> new UserNotFoundException(eleveId));
        if (eleve.getPromotion() != null && eleve.getPromotion().getId().equals(promotionId)) {
            eleve.setPromotion(null);
            userRepository.save(eleve);
        }
        return promotion;
    }

    @Transactional
    public CoursPlanifie createPlanning(Long promotionId, PlanningCreateRequest request, List<String> warnings) {
        Promotion promotion = findById(promotionId);
        Cours cours = coursRepository.findById(request.getCoursId())
                .orElseThrow(() -> new CoursNotFoundException(request.getCoursId()));

        List<CoursPlanifie> planning = coursPlanifieRepository.findByPromotionIdOrderByOrdre(promotionId);
        int ordre = planning.stream().mapToInt(CoursPlanifie::getOrdre).max().orElse(0) + 1;

        CoursPlanifie coursPlanifie = CoursPlanifie.builder()
                .promotion(promotion)
                .cours(cours)
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .ordre(ordre)
                .statut(CoursPlanifieStatut.PLANIFIE)
                .salle(request.getSalle())
                .build();

        if (request.getFormateurId() != null) {
            User formateur = userRepository.findById(request.getFormateurId())
                    .orElseThrow(() -> new UserNotFoundException(request.getFormateurId()));
            if (formateur.getRole() != Role.FORMATEUR) {
                throw new IllegalArgumentException(
                        "L'utilisateur avec l'id " + formateur.getUid() + " n'est pas un formateur.");
            }
            coursPlanifie.setFormateur(formateur);
        }

        CoursPlanifie saved = coursPlanifieRepository.save(coursPlanifie);

        detecterConflitsFormateur(promotionId, saved, warnings);

        return saved;
    }

    @Transactional
    public CoursPlanifie updatePlanning(Long promotionId, Long coursPlanifieId, PlanningUpdateRequest request, List<String> warnings) {
        findById(promotionId);
        CoursPlanifie coursPlanifie = coursPlanifieRepository.findById(coursPlanifieId)
                .orElseThrow(() -> new CoursPlanifieNotFoundException(coursPlanifieId));

        if (coursPlanifie.getPromotion() == null || !coursPlanifie.getPromotion().getId().equals(promotionId)) {
            throw new CoursPlanifieNotFoundException(coursPlanifieId);
        }

        coursPlanifie.setDateDebut(request.getDateDebut());
        coursPlanifie.setDateFin(request.getDateFin());
        coursPlanifie.setSalle(request.getSalle());

        if (request.getFormateurId() != null) {
            User formateur = userRepository.findById(request.getFormateurId())
                    .orElseThrow(() -> new UserNotFoundException(request.getFormateurId()));
            if (formateur.getRole() != Role.FORMATEUR) {
                throw new IllegalArgumentException(
                        "L'utilisateur avec l'id " + formateur.getUid() + " n'est pas un formateur.");
            }
            coursPlanifie.setFormateur(formateur);
        } else {
            coursPlanifie.setFormateur(null);
        }

        List<CoursPlanifie> planning = coursPlanifieRepository.findByPromotionIdOrderByOrdre(promotionId);
        planning.stream()
                .filter(pc -> pc.getOrdre() == coursPlanifie.getOrdre() - 1 || pc.getOrdre() == coursPlanifie.getOrdre() + 1)
                .filter(pc -> !pc.getId().equals(coursPlanifie.getId()))
                .filter(pc -> chevauchement(pc, coursPlanifie))
                .findAny()
                .ifPresent(_ -> warnings.add("ordre chronologique du cursus non respecté"));

        detecterConflitsFormateur(promotionId, coursPlanifie, warnings);

        return coursPlanifieRepository.save(coursPlanifie);
    }

    private void detecterConflitsFormateur(Long promotionId, CoursPlanifie coursPlanifie, List<String> warnings) {
        coursPlanifie.getCours().getFormateurs().forEach(formateur -> {
            List<CoursPlanifie> conflits = coursPlanifieRepository.findOverlappingForFormateur(
                    formateur.getUid(), coursPlanifie.getDateDebut(), coursPlanifie.getDateFin());
            conflits.stream()
                    .filter(pc -> !pc.getId().equals(coursPlanifie.getId()))
                    .filter(pc -> pc.getPromotion() != null && !pc.getPromotion().getId().equals(promotionId))
                    .forEach(pc -> warnings.add("Conflit formateur : " + formateur.getFirstName() + " " + formateur.getLastName()
                            + " déjà occupé du " + pc.getDateDebut() + " au " + pc.getDateFin()
                            + " sur la promotion " + pc.getPromotion().getName()));
        });

        if (coursPlanifie.getFormateur() != null) {
            User formateurAssigne = coursPlanifie.getFormateur();
            List<CoursPlanifie> conflits = coursPlanifieRepository.findOverlappingForFormateurAssigne(
                    formateurAssigne.getUid(), coursPlanifie.getDateDebut(), coursPlanifie.getDateFin());
            conflits.stream()
                    .filter(pc -> !pc.getId().equals(coursPlanifie.getId()))
                    .forEach(pc -> warnings.add("Conflit formateur : " + formateurAssigne.getFirstName() + " " + formateurAssigne.getLastName()
                            + " déjà occupé du " + pc.getDateDebut() + " au " + pc.getDateFin()
                            + (pc.getPromotion() != null ? " sur la promotion " + pc.getPromotion().getName() : " sur une autre session")));
        }
    }

    private boolean chevauchement(CoursPlanifie a, CoursPlanifie b) {
        return !a.getDateFin().isBefore(b.getDateDebut()) && !a.getDateDebut().isAfter(b.getDateFin());
    }

    private void affecterEleves(Promotion promotion, List<Long> eleveIds) {
        List<User> eleves = userRepository.findAllById(eleveIds);
        eleves.forEach(eleve -> eleve.setPromotion(promotion));
        userRepository.saveAll(eleves);
    }

    private Cursus findCursus(Long cursusId) {
        return cursusRepository.findById(cursusId)
                .orElseThrow(() -> new CursusNotFoundException(cursusId));
    }
}
