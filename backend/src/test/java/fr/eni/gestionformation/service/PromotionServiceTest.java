package fr.eni.gestionformation.service;

import fr.eni.gestionformation.dto.PlanningCreateRequest;
import fr.eni.gestionformation.dto.PlanningUpdateRequest;
import fr.eni.gestionformation.dto.PromotionRequest;
import fr.eni.gestionformation.dto.RythmeRequest;
import fr.eni.gestionformation.entity.Cours;
import fr.eni.gestionformation.entity.Cursus;
import fr.eni.gestionformation.entity.CursusCours;
import fr.eni.gestionformation.entity.Promotion;
import fr.eni.gestionformation.entity.CoursPlanifie;
import fr.eni.gestionformation.entity.CoursPlanifieStatut;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.exception.CoursNotFoundException;
import fr.eni.gestionformation.exception.CoursPlanifieNotFoundException;
import fr.eni.gestionformation.exception.PromotionNotFoundException;
import fr.eni.gestionformation.exception.UserNotFoundException;
import fr.eni.gestionformation.repository.CursusRepository;
import fr.eni.gestionformation.repository.CoursPlanifieRepository;
import fr.eni.gestionformation.repository.CoursRepository;
import fr.eni.gestionformation.repository.InscriptionCoursRepository;
import fr.eni.gestionformation.repository.PromotionRepository;
import fr.eni.gestionformation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    PromotionRepository promotionRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CursusRepository cursusRepository;

    @Mock
    CoursPlanifieRepository coursPlanifieRepository;

    @Mock
    CoursRepository coursRepository;

    @Mock
    InscriptionCoursRepository inscriptionCoursRepository;

    @Mock
    PlanificationService planificationService;

    @InjectMocks
    PromotionService promotionService;

    @Test
    void create_avecCursusEtRythmeEtEleves_genereLePlanning() {
        Cursus cursus = Cursus.builder().id(1L).name("Cursus").build();
        when(cursusRepository.findById(1L)).thenReturn(Optional.of(cursus));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> {
            Promotion p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });
        User eleve = User.builder().uid(5L).firstName("Jean").lastName("Dupont").build();
        when(userRepository.findAllById(List.of(5L))).thenReturn(List.of(eleve));

        PromotionRequest request = new PromotionRequest();
        request.setName("Promo A");
        request.setCursusId(1L);
        request.setDateDebut(LocalDate.of(2026, 6, 15));
        RythmeRequest rythmeRequest = new RythmeRequest();
        rythmeRequest.setSemainesCentre(3);
        rythmeRequest.setSemainesEntreprise(1);
        request.setRythme(rythmeRequest);
        request.setEleveIds(List.of(5L));

        Promotion result = promotionService.create(request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getCursus()).isEqualTo(cursus);
        assertThat(result.getRythme()).isNotNull();
        assertThat(result.getRythme().getSemainesCentre()).isEqualTo(3);
        assertThat(result.getRythme().getSemainesEntreprise()).isEqualTo(1);
        assertThat(eleve.getPromotion()).isEqualTo(result);
        verify(userRepository).saveAll(List.of(eleve));
        verify(planificationService).genererPlanning(result);
    }

    @Test
    void update_changementsSimples_neRegenerePasLePlanning() {
        Cursus cursus = Cursus.builder().id(1L).name("Cursus").build();
        Promotion existante = Promotion.builder().id(10L).name("Old").cursus(cursus).dateDebut(LocalDate.of(2026, 6, 15)).build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(existante));
        when(cursusRepository.findById(1L)).thenReturn(Optional.of(cursus));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PromotionRequest request = new PromotionRequest();
        request.setName("Nouveau nom");
        request.setCursusId(1L);
        request.setDateDebut(LocalDate.of(2026, 6, 15));

        Promotion result = promotionService.update(10L, request);

        assertThat(result.getName()).isEqualTo("Nouveau nom");
        verify(planificationService, org.mockito.Mockito.never()).genererPlanning(any());
        verify(coursPlanifieRepository, org.mockito.Mockito.never()).deleteAll(anyList());
    }

    @Test
    void update_eleveIds_remplaceCompletementLesEleves() {
        Cursus cursus = Cursus.builder().id(1L).name("Cursus").build();
        Promotion existante = Promotion.builder().id(10L).name("Promo").cursus(cursus).dateDebut(LocalDate.of(2026, 6, 15)).build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(existante));
        when(cursusRepository.findById(1L)).thenReturn(Optional.of(cursus));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User eleveRetire = User.builder().uid(5L).firstName("Jean").lastName("Dupont").promotion(existante).build();
        User eleveConserve = User.builder().uid(6L).firstName("Marie").lastName("Curie").promotion(existante).build();
        User eleveAjoute = User.builder().uid(7L).firstName("Paul").lastName("Martin").build();

        when(userRepository.findByPromotionId(10L)).thenReturn(List.of(eleveRetire, eleveConserve));
        when(userRepository.findAllById(List.of(6L, 7L))).thenReturn(List.of(eleveConserve, eleveAjoute));

        PromotionRequest request = new PromotionRequest();
        request.setName("Promo");
        request.setCursusId(1L);
        request.setDateDebut(LocalDate.of(2026, 6, 15));
        request.setEleveIds(List.of(6L, 7L));

        promotionService.update(10L, request);

        assertThat(eleveRetire.getPromotion()).isNull();
        assertThat(eleveConserve.getPromotion()).isEqualTo(existante);
        assertThat(eleveAjoute.getPromotion()).isEqualTo(existante);
        verify(userRepository).saveAll(List.of(eleveRetire));
        verify(userRepository).saveAll(List.of(eleveConserve, eleveAjoute));
    }

    @Test
    void update_changementDateDebut_regenereLePlanning() {
        Cursus cursus = Cursus.builder().id(1L).name("Cursus").build();
        Promotion existante = Promotion.builder().id(10L).name("Promo").cursus(cursus).dateDebut(LocalDate.of(2026, 6, 15)).build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(existante));
        when(cursusRepository.findById(1L)).thenReturn(Optional.of(cursus));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        List<CoursPlanifie> ancien = List.of(CoursPlanifie.builder().id(1L).build());
        when(coursPlanifieRepository.findByPromotionIdOrderByOrdre(10L)).thenReturn(ancien);

        PromotionRequest request = new PromotionRequest();
        request.setName("Promo");
        request.setCursusId(1L);
        request.setDateDebut(LocalDate.of(2026, 7, 1));

        promotionService.update(10L, request);

        verify(coursPlanifieRepository).deleteAll(ancien);
        verify(planificationService).genererPlanning(existante);
    }

    @Test
    void findById_inexistant_lancePromotionNotFound() {
        when(promotionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(PromotionNotFoundException.class, () -> promotionService.findById(99L));
    }

    @Test
    void createPlanning_promotionEtCoursExistants_creeUnCoursPlanifie() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo").build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(promotion));

        Cours cours = Cours.builder().id(5L).name("Cours A").build();
        when(coursRepository.findById(5L)).thenReturn(Optional.of(cours));

        CoursPlanifie existant = CoursPlanifie.builder()
                .id(100L).promotion(promotion).cours(cours)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(1).statut(CoursPlanifieStatut.PLANIFIE).build();
        when(coursPlanifieRepository.findByPromotionIdOrderByOrdre(10L)).thenReturn(List.of(existant));
        when(coursPlanifieRepository.save(any(CoursPlanifie.class))).thenAnswer(invocation -> {
            CoursPlanifie pc = invocation.getArgument(0);
            pc.setId(101L);
            return pc;
        });

        PlanningCreateRequest request = new PlanningCreateRequest();
        request.setCoursId(5L);
        request.setDateDebut(LocalDate.of(2026, 6, 22));
        request.setDateFin(LocalDate.of(2026, 6, 24));

        List<String> warnings = new ArrayList<>();
        CoursPlanifie result = promotionService.createPlanning(10L, request, warnings);

        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getOrdre()).isEqualTo(2);
        assertThat(result.getStatut()).isEqualTo(CoursPlanifieStatut.PLANIFIE);
        assertThat(result.getPromotion()).isEqualTo(promotion);
        assertThat(result.getCours()).isEqualTo(cours);
        assertThat(warnings).isEmpty();
    }

    @Test
    void createPlanning_coursInexistant_lanceCoursNotFound() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo").build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(promotion));
        when(coursRepository.findById(99L)).thenReturn(Optional.empty());

        PlanningCreateRequest request = new PlanningCreateRequest();
        request.setCoursId(99L);
        request.setDateDebut(LocalDate.of(2026, 6, 22));
        request.setDateFin(LocalDate.of(2026, 6, 24));

        List<String> warnings = new ArrayList<>();
        assertThrows(CoursNotFoundException.class, () -> promotionService.createPlanning(10L, request, warnings));
    }

    @Test
    void updatePlanning_sansConflit_aucunWarning() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo").build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(promotion));

        Cours cours = Cours.builder().id(1L).name("Cours A").build();
        CoursPlanifie coursPlanifie = CoursPlanifie.builder()
                .id(100L).promotion(promotion).cours(cours)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();
        when(coursPlanifieRepository.findById(100L)).thenReturn(Optional.of(coursPlanifie));
        when(coursPlanifieRepository.findByPromotionIdOrderByOrdre(10L)).thenReturn(List.of(coursPlanifie));
        when(coursPlanifieRepository.save(any(CoursPlanifie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanningUpdateRequest request = new PlanningUpdateRequest();
        request.setDateDebut(LocalDate.of(2026, 6, 22));
        request.setDateFin(LocalDate.of(2026, 6, 24));

        List<String> warnings = new ArrayList<>();
        CoursPlanifie result = promotionService.updatePlanning(10L, 100L, request, warnings);

        assertThat(result.getDateDebut()).isEqualTo(LocalDate.of(2026, 6, 22));
        assertThat(warnings).isEmpty();
    }

    @Test
    void updatePlanning_chevauchementAvecCoursSuivant_ajouteWarningOrdre() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo").build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(promotion));

        Cours coursA = Cours.builder().id(1L).name("Cours A").build();
        Cours coursB = Cours.builder().id(2L).name("Cours B").build();

        CoursPlanifie pcA = CoursPlanifie.builder()
                .id(100L).promotion(promotion).cours(coursA)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();
        CoursPlanifie pcB = CoursPlanifie.builder()
                .id(101L).promotion(promotion).cours(coursB)
                .dateDebut(LocalDate.of(2026, 6, 18)).dateFin(LocalDate.of(2026, 6, 20))
                .ordre(1).statut(CoursPlanifieStatut.PLANIFIE).build();

        when(coursPlanifieRepository.findById(100L)).thenReturn(Optional.of(pcA));
        when(coursPlanifieRepository.findByPromotionIdOrderByOrdre(10L)).thenReturn(List.of(pcA, pcB));
        when(coursPlanifieRepository.save(any(CoursPlanifie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanningUpdateRequest request = new PlanningUpdateRequest();
        request.setDateDebut(LocalDate.of(2026, 6, 16));
        request.setDateFin(LocalDate.of(2026, 6, 19));

        List<String> warnings = new ArrayList<>();
        promotionService.updatePlanning(10L, 100L, request, warnings);

        assertThat(warnings).contains("ordre chronologique du cursus non respecté");
    }

    @Test
    void updatePlanning_conflitFormateur_ajouteWarningConflit() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo A").build();
        Promotion autrePromotion = Promotion.builder().id(20L).name("Promo B").build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(promotion));

        User formateur = User.builder().uid(7L).firstName("Marie").lastName("Curie").build();
        Cours cours = Cours.builder().id(1L).name("Cours A").formateurs(new ArrayList<>(List.of(formateur))).build();

        CoursPlanifie coursPlanifie = CoursPlanifie.builder()
                .id(100L).promotion(promotion).cours(cours)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();
        when(coursPlanifieRepository.findById(100L)).thenReturn(Optional.of(coursPlanifie));
        when(coursPlanifieRepository.findByPromotionIdOrderByOrdre(10L)).thenReturn(List.of(coursPlanifie));
        when(coursPlanifieRepository.save(any(CoursPlanifie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CoursPlanifie conflit = CoursPlanifie.builder()
                .id(200L).promotion(autrePromotion).cours(cours)
                .dateDebut(LocalDate.of(2026, 6, 22)).dateFin(LocalDate.of(2026, 6, 24))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();
        when(coursPlanifieRepository.findOverlappingForFormateur(7L, LocalDate.of(2026, 6, 22), LocalDate.of(2026, 6, 24)))
                .thenReturn(List.of(conflit));

        PlanningUpdateRequest request = new PlanningUpdateRequest();
        request.setDateDebut(LocalDate.of(2026, 6, 22));
        request.setDateFin(LocalDate.of(2026, 6, 24));

        List<String> warnings = new ArrayList<>();
        promotionService.updatePlanning(10L, 100L, request, warnings);

        assertThat(warnings).anyMatch(w -> w.contains("Conflit formateur") && w.contains("Marie Curie") && w.contains("Promo B"));
    }

    @Test
    void updatePlanning_assigneFormateurEtSalle_metAJourCoursPlanifie() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo").build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(promotion));

        Cours cours = Cours.builder().id(1L).name("Cours A").build();
        CoursPlanifie coursPlanifie = CoursPlanifie.builder()
                .id(100L).promotion(promotion).cours(cours)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();
        when(coursPlanifieRepository.findById(100L)).thenReturn(Optional.of(coursPlanifie));
        when(coursPlanifieRepository.findByPromotionIdOrderByOrdre(10L)).thenReturn(List.of(coursPlanifie));
        when(coursPlanifieRepository.save(any(CoursPlanifie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User formateur = User.builder().uid(7L).firstName("Marie").lastName("Curie").role(Role.FORMATEUR).build();
        when(userRepository.findById(7L)).thenReturn(Optional.of(formateur));
        when(coursPlanifieRepository.findOverlappingForFormateurAssigne(7L, LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 17)))
                .thenReturn(List.of());

        PlanningUpdateRequest request = new PlanningUpdateRequest();
        request.setDateDebut(LocalDate.of(2026, 6, 15));
        request.setDateFin(LocalDate.of(2026, 6, 17));
        request.setFormateurId(7L);
        request.setSalle("Salle 12");

        List<String> warnings = new ArrayList<>();
        CoursPlanifie result = promotionService.updatePlanning(10L, 100L, request, warnings);

        assertThat(result.getFormateur()).isEqualTo(formateur);
        assertThat(result.getSalle()).isEqualTo("Salle 12");
        assertThat(warnings).isEmpty();
    }

    @Test
    void updatePlanning_formateurAssigneNonFormateur_lanceIllegalArgument() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo").build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(promotion));

        Cours cours = Cours.builder().id(1L).name("Cours A").build();
        CoursPlanifie coursPlanifie = CoursPlanifie.builder()
                .id(100L).promotion(promotion).cours(cours)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();
        when(coursPlanifieRepository.findById(100L)).thenReturn(Optional.of(coursPlanifie));

        User nonFormateur = User.builder().uid(8L).firstName("Paul").lastName("Martin").role(Role.ETUDIANT).build();
        when(userRepository.findById(8L)).thenReturn(Optional.of(nonFormateur));

        PlanningUpdateRequest request = new PlanningUpdateRequest();
        request.setDateDebut(LocalDate.of(2026, 6, 15));
        request.setDateFin(LocalDate.of(2026, 6, 17));
        request.setFormateurId(8L);

        assertThrows(IllegalArgumentException.class,
                () -> promotionService.updatePlanning(10L, 100L, request, new ArrayList<>()));
    }

    @Test
    void updatePlanning_conflitFormateurAssigneSurAutreSession_ajouteWarningConflit() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo A").build();
        Promotion autrePromotion = Promotion.builder().id(20L).name("Promo B").build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(promotion));

        Cours cours = Cours.builder().id(1L).name("Cours A").build();
        CoursPlanifie coursPlanifie = CoursPlanifie.builder()
                .id(100L).promotion(promotion).cours(cours)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();
        when(coursPlanifieRepository.findById(100L)).thenReturn(Optional.of(coursPlanifie));
        when(coursPlanifieRepository.findByPromotionIdOrderByOrdre(10L)).thenReturn(List.of(coursPlanifie));
        when(coursPlanifieRepository.save(any(CoursPlanifie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User formateur = User.builder().uid(7L).firstName("Marie").lastName("Curie").role(Role.FORMATEUR).build();
        when(userRepository.findById(7L)).thenReturn(Optional.of(formateur));

        Cours autreCours = Cours.builder().id(2L).name("Cours B").build();
        CoursPlanifie conflit = CoursPlanifie.builder()
                .id(200L).promotion(autrePromotion).cours(autreCours).formateur(formateur)
                .dateDebut(LocalDate.of(2026, 6, 16)).dateFin(LocalDate.of(2026, 6, 18))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();
        when(coursPlanifieRepository.findOverlappingForFormateurAssigne(7L, LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 17)))
                .thenReturn(List.of(conflit));

        PlanningUpdateRequest request = new PlanningUpdateRequest();
        request.setDateDebut(LocalDate.of(2026, 6, 15));
        request.setDateFin(LocalDate.of(2026, 6, 17));
        request.setFormateurId(7L);

        List<String> warnings = new ArrayList<>();
        promotionService.updatePlanning(10L, 100L, request, warnings);

        assertThat(warnings).anyMatch(w -> w.contains("Conflit formateur") && w.contains("Marie Curie") && w.contains("Promo B"));
    }

    @Test
    void updatePlanning_coursPlanifieInexistant_lanceCoursPlanifieNotFound() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo").build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(promotion));
        when(coursPlanifieRepository.findById(999L)).thenReturn(Optional.empty());

        PlanningUpdateRequest request = new PlanningUpdateRequest();
        request.setDateDebut(LocalDate.of(2026, 6, 22));
        request.setDateFin(LocalDate.of(2026, 6, 24));

        assertThrows(CoursPlanifieNotFoundException.class,
                () -> promotionService.updatePlanning(10L, 999L, request, new ArrayList<>()));
    }

    @Test
    void addEleve_affecteLEleveALaPromotion() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo").build();
        User eleve = User.builder().uid(5L).firstName("Jean").lastName("Dupont").build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(promotion));
        when(userRepository.findById(5L)).thenReturn(Optional.of(eleve));

        Promotion result = promotionService.addEleve(10L, 5L);

        assertThat(result).isEqualTo(promotion);
        assertThat(eleve.getPromotion()).isEqualTo(promotion);
        verify(userRepository).save(eleve);
    }

    @Test
    void addEleve_promotionInexistante_lancePromotionNotFound() {
        when(promotionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(PromotionNotFoundException.class, () -> promotionService.addEleve(99L, 5L));
    }

    @Test
    void addEleve_eleveInexistant_lanceUserNotFound() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo").build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(promotion));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> promotionService.addEleve(10L, 99L));
    }

    @Test
    void removeEleve_retireLEleveDeLaPromotion() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo").build();
        User eleve = User.builder().uid(5L).firstName("Jean").lastName("Dupont").promotion(promotion).build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(promotion));
        when(userRepository.findById(5L)).thenReturn(Optional.of(eleve));

        Promotion result = promotionService.removeEleve(10L, 5L);

        assertThat(result).isEqualTo(promotion);
        assertThat(eleve.getPromotion()).isNull();
        verify(userRepository).save(eleve);
    }

    @Test
    void removeEleve_eleveDansAutrePromotion_neModifieRien() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo").build();
        Promotion autrePromotion = Promotion.builder().id(20L).name("Autre Promo").build();
        User eleve = User.builder().uid(5L).firstName("Jean").lastName("Dupont").promotion(autrePromotion).build();
        when(promotionRepository.findById(10L)).thenReturn(Optional.of(promotion));
        when(userRepository.findById(5L)).thenReturn(Optional.of(eleve));

        promotionService.removeEleve(10L, 5L);

        assertThat(eleve.getPromotion()).isEqualTo(autrePromotion);
        verify(userRepository, org.mockito.Mockito.never()).save(eleve);
    }
}
