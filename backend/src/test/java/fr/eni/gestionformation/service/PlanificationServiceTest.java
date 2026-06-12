package fr.eni.gestionformation.service;

import fr.eni.gestionformation.entity.Cours;
import fr.eni.gestionformation.entity.Cursus;
import fr.eni.gestionformation.entity.CursusCours;
import fr.eni.gestionformation.entity.Promotion;
import fr.eni.gestionformation.entity.CoursPlanifie;
import fr.eni.gestionformation.entity.CoursPlanifieStatut;
import fr.eni.gestionformation.entity.Rythme;
import fr.eni.gestionformation.repository.CoursPlanifieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanificationServiceTest {

    @Mock
    CoursPlanifieRepository coursPlanifieRepository;

    @InjectMocks
    PlanificationService planificationService;

    private void mockSaveAll() {
        when(coursPlanifieRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void genererPlanning_sansRythme_joursOuvresSimples() {
        mockSaveAll();
        // Lundi 2026-06-15
        LocalDate dateDebut = LocalDate.of(2026, 6, 15);
        assertThat(dateDebut.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);

        Cours coursA = Cours.builder().id(1L).name("A").dureeJours(3).build();
        Cursus cursus = Cursus.builder().id(1L).name("Cursus").build();
        cursus.setCursusCours(List.of(
                CursusCours.builder().id(1L).cursus(cursus).cours(coursA).ordre(1).build()
        ));

        Promotion promotion = Promotion.builder().id(1L).name("Promo").cursus(cursus).dateDebut(dateDebut).build();

        List<CoursPlanifie> result = planificationService.genererPlanning(promotion);

        assertThat(result).hasSize(1);
        CoursPlanifie pc = result.get(0);
        assertThat(pc.getDateDebut()).isEqualTo(LocalDate.of(2026, 6, 15));
        // 3 jours ouvres consecutifs : lundi, mardi, mercredi
        assertThat(pc.getDateFin()).isEqualTo(LocalDate.of(2026, 6, 17));
        assertThat(pc.getStatut()).isEqualTo(CoursPlanifieStatut.PLANIFIE);
        assertThat(pc.getOrdre()).isEqualTo(1);
    }

    @Test
    void genererPlanning_avecRythme_excludesSemainesEntreprise() {
        mockSaveAll();
        // Lundi 2026-06-15, semaine 0 = centre, semaine 1 = entreprise (1 semaine centre, 1 semaine entreprise)
        LocalDate dateDebut = LocalDate.of(2026, 6, 15);

        // 6 jours -> remplit la semaine 0 (lun-ven = 5j) puis 1 jour de la semaine 2 (car semaine 1 = entreprise, exclue)
        Cours coursA = Cours.builder().id(1L).name("A").dureeJours(6).build();
        Cursus cursus = Cursus.builder().id(1L).name("Cursus").build();
        cursus.setCursusCours(List.of(
                CursusCours.builder().id(1L).cursus(cursus).cours(coursA).ordre(1).build()
        ));

        Rythme rythme = Rythme.builder().id(1L).semainesCentre(1).semainesEntreprise(1).build();
        Promotion promotion = Promotion.builder().id(1L).name("Promo").cursus(cursus).dateDebut(dateDebut).rythme(rythme).build();

        List<CoursPlanifie> result = planificationService.genererPlanning(promotion);

        assertThat(result).hasSize(1);
        CoursPlanifie pc = result.get(0);
        assertThat(pc.getDateDebut()).isEqualTo(LocalDate.of(2026, 6, 15));
        // semaine 0 (centre): 15-19 juin (5 jours), semaine 1 (entreprise) sautee entierement,
        // semaine 2 (centre): premier jour valide = 29 juin -> 6e jour
        assertThat(pc.getDateFin()).isEqualTo(LocalDate.of(2026, 6, 29));
    }

    @Test
    void genererPlanning_dureeJoursNull_defaultUnJour() {
        mockSaveAll();
        LocalDate dateDebut = LocalDate.of(2026, 6, 15);

        Cours coursA = Cours.builder().id(1L).name("A").dureeJours(null).build();
        Cursus cursus = Cursus.builder().id(1L).name("Cursus").build();
        cursus.setCursusCours(List.of(
                CursusCours.builder().id(1L).cursus(cursus).cours(coursA).ordre(1).build()
        ));

        Promotion promotion = Promotion.builder().id(1L).name("Promo").cursus(cursus).dateDebut(dateDebut).build();

        List<CoursPlanifie> result = planificationService.genererPlanning(promotion);

        assertThat(result).hasSize(1);
        CoursPlanifie pc = result.get(0);
        assertThat(pc.getDateDebut()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(pc.getDateFin()).isEqualTo(LocalDate.of(2026, 6, 15));
    }

    @Test
    void genererPlanning_sequenceDePlusieursCours_sansChevauchement() {
        mockSaveAll();
        LocalDate dateDebut = LocalDate.of(2026, 6, 15);

        Cours coursA = Cours.builder().id(1L).name("A").dureeJours(2).build();
        Cours coursB = Cours.builder().id(2L).name("B").dureeJours(3).build();
        Cursus cursus = Cursus.builder().id(1L).name("Cursus").build();
        cursus.setCursusCours(List.of(
                CursusCours.builder().id(1L).cursus(cursus).cours(coursA).ordre(1).build(),
                CursusCours.builder().id(2L).cursus(cursus).cours(coursB).ordre(2).build()
        ));

        Promotion promotion = Promotion.builder().id(1L).name("Promo").cursus(cursus).dateDebut(dateDebut).build();

        List<CoursPlanifie> result = planificationService.genererPlanning(promotion);

        assertThat(result).hasSize(2);
        CoursPlanifie pcA = result.get(0);
        CoursPlanifie pcB = result.get(1);

        // Cours A : lundi 15 -> mardi 16 (2 jours)
        assertThat(pcA.getDateDebut()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(pcA.getDateFin()).isEqualTo(LocalDate.of(2026, 6, 16));

        // Cours B : mercredi 17 -> vendredi 19 (3 jours), strictement apres A, pas de chevauchement
        assertThat(pcB.getDateDebut()).isEqualTo(LocalDate.of(2026, 6, 17));
        assertThat(pcB.getDateFin()).isEqualTo(LocalDate.of(2026, 6, 19));

        assertThat(pcA.getDateFin()).isBefore(pcB.getDateDebut());
    }
}
