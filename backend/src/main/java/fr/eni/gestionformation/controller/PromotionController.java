package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.CoursPlanifieResponse;
import fr.eni.gestionformation.dto.EleveInfo;
import fr.eni.gestionformation.dto.PlanningCreateRequest;
import fr.eni.gestionformation.dto.PlanningUpdateRequest;
import fr.eni.gestionformation.dto.PromotionRequest;
import fr.eni.gestionformation.dto.PromotionResponse;
import fr.eni.gestionformation.dto.RythmeResponse;
import fr.eni.gestionformation.entity.CoursPlanifie;
import fr.eni.gestionformation.entity.Promotion;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    public ResponseEntity<List<PromotionResponse>> getAll() {
        return ResponseEntity.ok(promotionService.findAll().stream()
                .map(this::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(promotionService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<PromotionResponse> create(@RequestBody PromotionRequest request) {
        Promotion saved = promotionService.create(request);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> update(@PathVariable Long id, @RequestBody PromotionRequest request) {
        Promotion saved = promotionService.update(id, request);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promotionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/eleves/{eleveId}")
    public ResponseEntity<PromotionResponse> addEleve(@PathVariable Long id, @PathVariable Long eleveId) {
        Promotion saved = promotionService.addEleve(id, eleveId);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}/eleves/{eleveId}")
    public ResponseEntity<PromotionResponse> removeEleve(@PathVariable Long id, @PathVariable Long eleveId) {
        Promotion saved = promotionService.removeEleve(id, eleveId);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PostMapping("/{id}/planning")
    public ResponseEntity<CoursPlanifieResponse> createPlanning(@PathVariable Long id,
                                                                  @RequestBody PlanningCreateRequest request) {
        List<String> warnings = new ArrayList<>();
        CoursPlanifie coursPlanifie = promotionService.createPlanning(id, request, warnings);
        return ResponseEntity.ok(toCoursPlanifieResponse(coursPlanifie, warnings));
    }

    @PutMapping("/{id}/planning/{coursPlanifieId}")
    public ResponseEntity<CoursPlanifieResponse> updatePlanning(@PathVariable Long id,
                                                                   @PathVariable Long coursPlanifieId,
                                                                   @RequestBody PlanningUpdateRequest request) {
        List<String> warnings = new ArrayList<>();
        CoursPlanifie coursPlanifie = promotionService.updatePlanning(id, coursPlanifieId, request, warnings);
        return ResponseEntity.ok(toCoursPlanifieResponse(coursPlanifie, warnings));
    }

    @DeleteMapping("/{id}/planning/{coursPlanifieId}")
    public ResponseEntity<Void> deletePlanning(@PathVariable Long id, @PathVariable Long coursPlanifieId) {
        promotionService.deletePlanning(id, coursPlanifieId);
        return ResponseEntity.noContent().build();
    }

    private PromotionResponse toResponse(Promotion promotion) {
        Long cursusId = promotion.getCursus() != null ? promotion.getCursus().getId() : null;
        String cursusNom = promotion.getCursus() != null ? promotion.getCursus().getName() : null;
        RythmeResponse rythme = promotion.getRythme() != null
                ? new RythmeResponse(promotion.getRythme().getSemainesCentre(), promotion.getRythme().getSemainesEntreprise())
                : null;
        List<EleveInfo> eleves = promotionService.getEleves(promotion.getId()).stream()
                .map(this::toEleveInfo)
                .toList();
        List<CoursPlanifieResponse> planning = promotionService.getPlanning(promotion.getId()).stream()
                .map(pc -> toCoursPlanifieResponse(pc, List.of()))
                .toList();
        return new PromotionResponse(promotion.getId(), promotion.getName(), cursusId, cursusNom,
                promotion.getDateDebut(), rythme, eleves, planning);
    }

    private EleveInfo toEleveInfo(User user) {
        return new EleveInfo(user.getUid(), user.getFirstName(), user.getLastName());
    }

    private CoursPlanifieResponse toCoursPlanifieResponse(CoursPlanifie pc, List<String> warnings) {
        Long formateurId = pc.getFormateur() != null ? pc.getFormateur().getUid() : null;
        String formateurNom = pc.getFormateur() != null
                ? pc.getFormateur().getFirstName() + " " + pc.getFormateur().getLastName()
                : null;
        return new CoursPlanifieResponse(pc.getId(), pc.getCours().getId(), pc.getCours().getName(),
                pc.getDateDebut(), pc.getDateFin(), pc.getOrdre(), pc.getStatut(),
                formateurId, formateurNom, pc.getSalle(), warnings);
    }
}
