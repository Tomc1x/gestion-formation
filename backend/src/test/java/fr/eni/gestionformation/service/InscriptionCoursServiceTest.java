package fr.eni.gestionformation.service;

import fr.eni.gestionformation.dto.OrigineInscription;
import fr.eni.gestionformation.entity.Cours;
import fr.eni.gestionformation.entity.CoursPlanifie;
import fr.eni.gestionformation.entity.CoursPlanifieStatut;
import fr.eni.gestionformation.entity.Cursus;
import fr.eni.gestionformation.entity.CursusCours;
import fr.eni.gestionformation.entity.InscriptionCours;
import fr.eni.gestionformation.entity.Promotion;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.exception.InscriptionAlreadyExistsException;
import fr.eni.gestionformation.exception.InscriptionHorsOrdreException;
import fr.eni.gestionformation.repository.CoursPlanifieRepository;
import fr.eni.gestionformation.repository.CursusCoursRepository;
import fr.eni.gestionformation.repository.InscriptionCoursRepository;
import fr.eni.gestionformation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InscriptionCoursServiceTest {

    @Mock
    InscriptionCoursRepository inscriptionCoursRepository;

    @Mock
    CoursPlanifieRepository coursPlanifieRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CursusCoursRepository cursusCoursRepository;

    @InjectMocks
    InscriptionCoursService inscriptionCoursService;

    @Test
    void creerInscription_eleveHorsPromotion_creeInscription() {
        Cours cours = Cours.builder().id(1L).name("Cours A").build();
        CoursPlanifie coursPlanifie = CoursPlanifie.builder()
                .id(100L).cours(cours).promotion(null)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();
        User eleve = User.builder().uid(5L).firstName("Jean").lastName("Dupont").build();

        when(coursPlanifieRepository.findById(100L)).thenReturn(Optional.of(coursPlanifie));
        when(userRepository.findById(5L)).thenReturn(Optional.of(eleve));
        when(inscriptionCoursRepository.existsByEleveUidAndCoursPlanifieId(5L, 100L)).thenReturn(false);
        when(inscriptionCoursRepository.save(any(InscriptionCours.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InscriptionCoursService.InscriptionResult result = inscriptionCoursService.creerInscription(100L, 5L, false);

        assertThat(result.inscription().getEleve()).isEqualTo(eleve);
        assertThat(result.inscription().getCoursPlanifie()).isEqualTo(coursPlanifie);
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void creerInscription_eleveDejaCouvertParSaPromotion_lanceConflit() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo A").build();
        Cours cours = Cours.builder().id(1L).name("Cours A").build();
        CoursPlanifie coursPlanifie = CoursPlanifie.builder()
                .id(100L).cours(cours).promotion(promotion)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();
        User eleve = User.builder().uid(5L).firstName("Jean").lastName("Dupont").promotion(promotion).build();

        when(coursPlanifieRepository.findById(100L)).thenReturn(Optional.of(coursPlanifie));
        when(userRepository.findById(5L)).thenReturn(Optional.of(eleve));

        assertThrows(InscriptionAlreadyExistsException.class,
                () -> inscriptionCoursService.creerInscription(100L, 5L, false));
    }

    @Test
    void creerInscription_dejaInscritIndividuellement_lanceConflit() {
        Cours cours = Cours.builder().id(1L).name("Cours A").build();
        CoursPlanifie coursPlanifie = CoursPlanifie.builder()
                .id(100L).cours(cours).promotion(null)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();
        User eleve = User.builder().uid(5L).firstName("Jean").lastName("Dupont").build();

        when(coursPlanifieRepository.findById(100L)).thenReturn(Optional.of(coursPlanifie));
        when(userRepository.findById(5L)).thenReturn(Optional.of(eleve));
        when(inscriptionCoursRepository.existsByEleveUidAndCoursPlanifieId(5L, 100L)).thenReturn(true);

        assertThrows(InscriptionAlreadyExistsException.class,
                () -> inscriptionCoursService.creerInscription(100L, 5L, false));
    }

    @Test
    void getInscritsCombines_promoEtIndividuel_dedupliqueEtMarqueOrigine() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo A").build();
        Cours cours = Cours.builder().id(1L).name("Cours A").build();
        CoursPlanifie coursPlanifie = CoursPlanifie.builder()
                .id(100L).cours(cours).promotion(promotion)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();

        User elevePromo = User.builder().uid(5L).firstName("Jean").lastName("Dupont").promotion(promotion).build();
        User eleveIndividuel = User.builder().uid(6L).firstName("Marie").lastName("Curie").build();

        when(coursPlanifieRepository.findById(100L)).thenReturn(Optional.of(coursPlanifie));
        when(userRepository.findByPromotionId(10L)).thenReturn(List.of(elevePromo));
        InscriptionCours inscriptionIndividuelle = InscriptionCours.builder()
                .id(1L).eleve(eleveIndividuel).coursPlanifie(coursPlanifie).dateInscription(LocalDate.now()).build();
        when(inscriptionCoursRepository.findByCoursPlanifieId(100L)).thenReturn(List.of(inscriptionIndividuelle));

        Map<User, OrigineInscription> result = inscriptionCoursService.getInscritsCombines(100L);

        assertThat(result).hasSize(2);
        assertThat(result.get(elevePromo)).isEqualTo(OrigineInscription.PROMOTION);
        assertThat(result.get(eleveIndividuel)).isEqualTo(OrigineInscription.INDIVIDUEL);
    }

    @Test
    void getInscritsCombines_coursSansPromotion_neContientQueLesIndividuels() {
        Cours cours = Cours.builder().id(1L).name("Cours A").build();
        CoursPlanifie coursPlanifie = CoursPlanifie.builder()
                .id(100L).cours(cours).promotion(null)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();
        User eleveIndividuel = User.builder().uid(6L).firstName("Marie").lastName("Curie").build();

        when(coursPlanifieRepository.findById(100L)).thenReturn(Optional.of(coursPlanifie));
        InscriptionCours inscriptionIndividuelle = InscriptionCours.builder()
                .id(1L).eleve(eleveIndividuel).coursPlanifie(coursPlanifie).dateInscription(LocalDate.now()).build();
        when(inscriptionCoursRepository.findByCoursPlanifieId(100L)).thenReturn(List.of(inscriptionIndividuelle));

        Map<User, OrigineInscription> result = inscriptionCoursService.getInscritsCombines(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(eleveIndividuel)).isEqualTo(OrigineInscription.INDIVIDUEL);
    }

    @Test
    void getPlanningEleve_promoEtIndividuel_dedupliqueEtMarqueOrigine() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo A").build();
        User eleve = User.builder().uid(5L).firstName("Jean").lastName("Dupont").promotion(promotion).build();

        Cours coursA = Cours.builder().id(1L).name("Cours A").build();
        Cours coursB = Cours.builder().id(2L).name("Cours B").build();
        CoursPlanifie coursPromo = CoursPlanifie.builder()
                .id(100L).cours(coursA).promotion(promotion)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();
        CoursPlanifie coursIndividuel = CoursPlanifie.builder()
                .id(200L).cours(coursB).promotion(null)
                .dateDebut(LocalDate.of(2026, 7, 1)).dateFin(LocalDate.of(2026, 7, 2))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(eleve));
        when(coursPlanifieRepository.findByPromotionIdOrderByOrdre(10L)).thenReturn(List.of(coursPromo));
        InscriptionCours inscription = InscriptionCours.builder()
                .id(1L).eleve(eleve).coursPlanifie(coursIndividuel).dateInscription(LocalDate.now()).build();
        when(inscriptionCoursRepository.findByEleveUid(5L)).thenReturn(List.of(inscription));

        Map<CoursPlanifie, OrigineInscription> result = inscriptionCoursService.getPlanningEleve(5L);

        assertThat(result).hasSize(2);
        assertThat(result.get(coursPromo)).isEqualTo(OrigineInscription.PROMOTION);
        assertThat(result.get(coursIndividuel)).isEqualTo(OrigineInscription.INDIVIDUEL);
    }

    @Test
    void getPlanningEleve_inscriptionIndividuelleSurCoursDeSaProprePromotion_neDuplicePas() {
        Promotion promotion = Promotion.builder().id(10L).name("Promo A").build();
        User eleve = User.builder().uid(5L).firstName("Jean").lastName("Dupont").promotion(promotion).build();

        Cours coursA = Cours.builder().id(1L).name("Cours A").build();
        CoursPlanifie coursPromo = CoursPlanifie.builder()
                .id(100L).cours(coursA).promotion(promotion)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(eleve));
        when(coursPlanifieRepository.findByPromotionIdOrderByOrdre(10L)).thenReturn(List.of(coursPromo));
        InscriptionCours inscription = InscriptionCours.builder()
                .id(1L).eleve(eleve).coursPlanifie(coursPromo).dateInscription(LocalDate.now()).build();
        when(inscriptionCoursRepository.findByEleveUid(5L)).thenReturn(List.of(inscription));

        Map<CoursPlanifie, OrigineInscription> result = inscriptionCoursService.getPlanningEleve(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(coursPromo)).isEqualTo(OrigineInscription.PROMOTION);
    }

    @Test
    void creerInscription_horsOrdreSansForcer_lanceException() {
        Cursus cursus = Cursus.builder().id(1L).name("Cursus A").build();
        Promotion promotion = Promotion.builder().id(10L).name("Promo A").cursus(cursus).build();
        User eleve = User.builder().uid(5L).firstName("Jean").lastName("Dupont").promotion(promotion).build();

        Cours coursPrerequis = Cours.builder().id(1L).name("Cours Prerequis").build();
        Cours coursCible = Cours.builder().id(2L).name("Cours Cible").build();

        CursusCours ccPrerequis = CursusCours.builder().id(1L).cursus(cursus).cours(coursPrerequis).ordre(0).build();
        CursusCours ccCible = CursusCours.builder().id(2L).cursus(cursus).cours(coursCible).ordre(1).build();

        CoursPlanifie coursPlanifieCible = CoursPlanifie.builder()
                .id(200L).cours(coursCible).promotion(null)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();

        when(coursPlanifieRepository.findById(200L)).thenReturn(Optional.of(coursPlanifieCible));
        when(userRepository.findById(5L)).thenReturn(Optional.of(eleve));
        when(inscriptionCoursRepository.existsByEleveUidAndCoursPlanifieId(5L, 200L)).thenReturn(false);
        when(cursusCoursRepository.findByCursusIdOrderByOrdre(1L)).thenReturn(List.of(ccPrerequis, ccCible));
        when(coursPlanifieRepository.findByPromotionIdOrderByOrdre(10L)).thenReturn(List.of());
        when(inscriptionCoursRepository.findByEleveUid(5L)).thenReturn(List.of());

        assertThrows(InscriptionHorsOrdreException.class,
                () -> inscriptionCoursService.creerInscription(200L, 5L, false));
    }

    @Test
    void creerInscription_horsOrdreAvecForcer_succesAvecWarnings() {
        Cursus cursus = Cursus.builder().id(1L).name("Cursus A").build();
        Promotion promotion = Promotion.builder().id(10L).name("Promo A").cursus(cursus).build();
        User eleve = User.builder().uid(5L).firstName("Jean").lastName("Dupont").promotion(promotion).build();

        Cours coursPrerequis = Cours.builder().id(1L).name("Cours Prerequis").build();
        Cours coursCible = Cours.builder().id(2L).name("Cours Cible").build();

        CursusCours ccPrerequis = CursusCours.builder().id(1L).cursus(cursus).cours(coursPrerequis).ordre(0).build();
        CursusCours ccCible = CursusCours.builder().id(2L).cursus(cursus).cours(coursCible).ordre(1).build();

        CoursPlanifie coursPlanifieCible = CoursPlanifie.builder()
                .id(200L).cours(coursCible).promotion(null)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();

        when(coursPlanifieRepository.findById(200L)).thenReturn(Optional.of(coursPlanifieCible));
        when(userRepository.findById(5L)).thenReturn(Optional.of(eleve));
        when(inscriptionCoursRepository.existsByEleveUidAndCoursPlanifieId(5L, 200L)).thenReturn(false);
        when(cursusCoursRepository.findByCursusIdOrderByOrdre(1L)).thenReturn(List.of(ccPrerequis, ccCible));
        when(coursPlanifieRepository.findByPromotionIdOrderByOrdre(10L)).thenReturn(List.of());
        when(inscriptionCoursRepository.findByEleveUid(5L)).thenReturn(List.of());
        when(inscriptionCoursRepository.save(any(InscriptionCours.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InscriptionCoursService.InscriptionResult result = inscriptionCoursService.creerInscription(200L, 5L, true);

        assertThat(result.inscription().getEleve()).isEqualTo(eleve);
        assertThat(result.warnings()).hasSize(1);
        assertThat(result.warnings().get(0)).contains("hors-ordre");
        assertThat(result.warnings().get(0)).contains("Cours Prerequis");
    }

    @Test
    void creerInscription_enOrdre_succesSansWarnings() {
        Cursus cursus = Cursus.builder().id(1L).name("Cursus A").build();
        Promotion promotion = Promotion.builder().id(10L).name("Promo A").cursus(cursus).build();
        User eleve = User.builder().uid(5L).firstName("Jean").lastName("Dupont").promotion(promotion).build();

        Cours coursPrerequis = Cours.builder().id(1L).name("Cours Prerequis").build();
        Cours coursCible = Cours.builder().id(2L).name("Cours Cible").build();

        CursusCours ccPrerequis = CursusCours.builder().id(1L).cursus(cursus).cours(coursPrerequis).ordre(0).build();
        CursusCours ccCible = CursusCours.builder().id(2L).cursus(cursus).cours(coursCible).ordre(1).build();

        CoursPlanifie coursPlanifiePrerequis = CoursPlanifie.builder()
                .id(199L).cours(coursPrerequis).promotion(promotion)
                .dateDebut(LocalDate.of(2026, 5, 1)).dateFin(LocalDate.of(2026, 5, 3))
                .ordre(0).statut(CoursPlanifieStatut.TERMINE).build();
        CoursPlanifie coursPlanifieCible = CoursPlanifie.builder()
                .id(200L).cours(coursCible).promotion(null)
                .dateDebut(LocalDate.of(2026, 6, 15)).dateFin(LocalDate.of(2026, 6, 17))
                .ordre(0).statut(CoursPlanifieStatut.PLANIFIE).build();

        when(coursPlanifieRepository.findById(200L)).thenReturn(Optional.of(coursPlanifieCible));
        when(userRepository.findById(5L)).thenReturn(Optional.of(eleve));
        when(inscriptionCoursRepository.existsByEleveUidAndCoursPlanifieId(5L, 200L)).thenReturn(false);
        when(cursusCoursRepository.findByCursusIdOrderByOrdre(1L)).thenReturn(List.of(ccPrerequis, ccCible));
        when(coursPlanifieRepository.findByPromotionIdOrderByOrdre(10L)).thenReturn(List.of(coursPlanifiePrerequis));
        when(inscriptionCoursRepository.findByEleveUid(5L)).thenReturn(List.of());
        when(inscriptionCoursRepository.save(any(InscriptionCours.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InscriptionCoursService.InscriptionResult result = inscriptionCoursService.creerInscription(200L, 5L, false);

        assertThat(result.inscription().getEleve()).isEqualTo(eleve);
        assertThat(result.warnings()).isEmpty();
    }
}
