package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.InscriptionCoursRequest;
import fr.eni.gestionformation.dto.InscriptionCoursResponse;
import fr.eni.gestionformation.dto.InscritResponse;
import fr.eni.gestionformation.dto.OrigineInscription;
import fr.eni.gestionformation.dto.PlanningEleveResponse;
import fr.eni.gestionformation.dto.PlanningFormateurResponse;
import fr.eni.gestionformation.entity.CoursPlanifie;
import fr.eni.gestionformation.entity.InscriptionCours;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.service.InscriptionCoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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
        InscriptionCoursService.InscriptionResult result = inscriptionCoursService.creerInscription(id, request.getEleveId(), request.isForcer());
        return ResponseEntity.status(HttpStatus.CREATED).body(toInscriptionResponse(result));
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
    public ResponseEntity<List<PlanningEleveResponse>> getPlanningEleve(@PathVariable Long id, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User currentUser) || !currentUser.getUid().equals(id)) {
            throw new AccessDeniedException("Accès non autorisé au planning de cet élève");
        }
        Map<CoursPlanifie, OrigineInscription> planning = inscriptionCoursService.getPlanningEleve(id);
        List<PlanningEleveResponse> reponse = planning.entrySet().stream()
                .map(entry -> {
                    CoursPlanifie cp = entry.getKey();
                    User formateur = cp.getFormateur();
                    Long formateurId = formateur != null ? formateur.getUid() : null;
                    String formateurNom = formateur != null
                            ? (formateur.getFirstName() + " " + formateur.getLastName())
                            : "Non assigné";
                    return new PlanningEleveResponse(cp.getId(), cp.getCours().getId(), cp.getCours().getName(),
                            cp.getDateDebut(), cp.getDateFin(), cp.getOrdre(), cp.getStatut(), entry.getValue(),
                            formateurId, formateurNom);
                })
                .toList();
        return ResponseEntity.ok(reponse);
    }

    @GetMapping("/api/formateurs/{id}/planning")
    public ResponseEntity<List<PlanningFormateurResponse>> getPlanningFormateur(@PathVariable Long id, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User currentUser)
                || (!currentUser.getUid().equals(id) && !hasRole(authentication, "ROLE_ADMINISTRATEUR", "ROLE_REFERENTE_ADMINISTRATIVE"))) {
            throw new AccessDeniedException("Accès non autorisé au planning de ce formateur");
        }
        List<CoursPlanifie> planning = inscriptionCoursService.getPlanningFormateur(id);
        List<PlanningFormateurResponse> reponse = planning.stream()
                .map(cp -> {
                    List<InscritResponse> eleves = inscriptionCoursService.getInscritsCombines(cp.getId()).entrySet().stream()
                            .map(entry -> new InscritResponse(entry.getKey().getUid(), entry.getKey().getFirstName(),
                                    entry.getKey().getLastName(), entry.getValue()))
                            .toList();
                    return new PlanningFormateurResponse(cp.getId(), cp.getCours().getId(), cp.getCours().getName(),
                            cp.getDateDebut(), cp.getDateFin(), cp.getOrdre(), cp.getStatut(), cp.getSalle(), eleves);
                })
                .toList();
        return ResponseEntity.ok(reponse);
    }

    private boolean hasRole(Authentication authentication, String... roles) {
        for (String role : roles) {
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role))) {
                return true;
            }
        }
        return false;
    }

    private InscriptionCoursResponse toInscriptionResponse(InscriptionCoursService.InscriptionResult result) {
        InscriptionCours inscription = result.inscription();
        return new InscriptionCoursResponse(inscription.getId(), inscription.getEleve().getUid(),
                inscription.getCoursPlanifie().getId(), inscription.getDateInscription(), result.warnings());
    }
}
