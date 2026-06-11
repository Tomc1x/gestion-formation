package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.InscriptionCoursRequest;
import fr.eni.gestionformation.dto.InscriptionCoursResponse;
import fr.eni.gestionformation.dto.InscritResponse;
import fr.eni.gestionformation.dto.OrigineInscription;
import fr.eni.gestionformation.dto.PlanningEleveResponse;
import fr.eni.gestionformation.entity.CoursPlanifie;
import fr.eni.gestionformation.entity.InscriptionCours;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.service.InscriptionCoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class InscriptionCoursController {

    private final InscriptionCoursService inscriptionCoursService;

    @PostMapping("/api/cours-planifies/{id}/inscriptions")
    public ResponseEntity<InscriptionCoursResponse> creerInscription(@PathVariable Long id,
                                                                        @RequestBody InscriptionCoursRequest request) {
        InscriptionCours inscription = inscriptionCoursService.creerInscription(id, request.getEleveId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toInscriptionResponse(inscription));
    }

    @DeleteMapping("/api/cours-planifies/{id}/inscriptions/{eleveId}")
    public ResponseEntity<Void> supprimerInscription(@PathVariable Long id, @PathVariable Long eleveId) {
        inscriptionCoursService.supprimerInscription(id, eleveId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/cours-planifies/{id}/inscrits")
    public ResponseEntity<List<InscritResponse>> getInscrits(@PathVariable Long id) {
        Map<User, OrigineInscription> inscrits = inscriptionCoursService.getInscritsCombines(id);
        List<InscritResponse> reponse = inscrits.entrySet().stream()
                .map(entry -> new InscritResponse(entry.getKey().getUid(), entry.getKey().getFirstName(),
                        entry.getKey().getLastName(), entry.getValue()))
                .toList();
        return ResponseEntity.ok(reponse);
    }

    @GetMapping("/api/eleves/{id}/planning")
    public ResponseEntity<List<PlanningEleveResponse>> getPlanningEleve(@PathVariable Long id) {
        Map<CoursPlanifie, OrigineInscription> planning = inscriptionCoursService.getPlanningEleve(id);
        List<PlanningEleveResponse> reponse = planning.entrySet().stream()
                .map(entry -> {
                    CoursPlanifie cp = entry.getKey();
                    return new PlanningEleveResponse(cp.getId(), cp.getCours().getId(), cp.getCours().getName(),
                            cp.getDateDebut(), cp.getDateFin(), cp.getOrdre(), cp.getStatut(), entry.getValue());
                })
                .toList();
        return ResponseEntity.ok(reponse);
    }

    private InscriptionCoursResponse toInscriptionResponse(InscriptionCours inscription) {
        return new InscriptionCoursResponse(inscription.getId(), inscription.getEleve().getUid(),
                inscription.getCoursPlanifie().getId(), inscription.getDateInscription());
    }
}
