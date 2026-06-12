package fr.eni.gestionformation.service;

import fr.eni.gestionformation.entity.CoursPlanifie;
import fr.eni.gestionformation.entity.CoursPlanifieStatut;
import fr.eni.gestionformation.entity.CursusCours;
import fr.eni.gestionformation.entity.Promotion;
import fr.eni.gestionformation.entity.Rythme;
import fr.eni.gestionformation.repository.CoursPlanifieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanificationService {

    private final CoursPlanifieRepository coursPlanifieRepository;

    public List<CoursPlanifie> genererPlanning(Promotion promotion) {
        List<CoursPlanifie> resultat = new ArrayList<>();
        if (promotion.getCursus() == null) {
            return resultat;
        }

        Rythme rythme = promotion.getRythme();
        LocalDate curseur = premierJourValide(promotion.getDateDebut(), promotion, rythme);

        for (CursusCours cursusCours : promotion.getCursus().getCursusCours()) {
            int dureeJours = cursusCours.getCours().getDureeJours() != null
                    ? cursusCours.getCours().getDureeJours()
                    : 1;

            LocalDate dateDebut = curseur;
            LocalDate dateFin = dateDebut;
            for (int i = 1; i < dureeJours; i++) {
                dateFin = jourValideSuivant(dateFin, promotion, rythme);
            }

            CoursPlanifie pc = CoursPlanifie.builder()
                    .promotion(promotion)
                    .cours(cursusCours.getCours())
                    .dateDebut(dateDebut)
                    .dateFin(dateFin)
                    .ordre(cursusCours.getOrdre())
                    .statut(CoursPlanifieStatut.PLANIFIE)
                    .build();
            resultat.add(pc);

            curseur = jourValideSuivant(dateFin, promotion, rythme);
        }

        return coursPlanifieRepository.saveAll(resultat);
    }

    private LocalDate premierJourValide(LocalDate date, Promotion promotion, Rythme rythme) {
        LocalDate candidat = date;
        while (!estJourValide(candidat, promotion, rythme)) {
            candidat = candidat.plusDays(1);
        }
        return candidat;
    }

    private LocalDate jourValideSuivant(LocalDate date, Promotion promotion, Rythme rythme) {
        return premierJourValide(date.plusDays(1), promotion, rythme);
    }

    private boolean estJourValide(LocalDate date, Promotion promotion, Rythme rythme) {
        DayOfWeek jour = date.getDayOfWeek();
        if (jour == DayOfWeek.SATURDAY || jour == DayOfWeek.SUNDAY) {
            return false;
        }
        if (rythme == null) {
            return true;
        }
        long semainesEcoulees = ChronoUnit.WEEKS.between(promotion.getDateDebut(), date);
        int cycle = rythme.getSemainesCentre() + rythme.getSemainesEntreprise();
        if (cycle <= 0) {
            return true;
        }
        long numeroSemaine = Math.floorMod(semainesEcoulees, cycle);
        return numeroSemaine < rythme.getSemainesCentre();
    }
}
