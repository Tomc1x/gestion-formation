package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.CoursRequest;
import fr.eni.gestionformation.dto.CoursResponse;
import fr.eni.gestionformation.dto.FormateurInfo;
import fr.eni.gestionformation.entity.Cours;
import fr.eni.gestionformation.service.CoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class CoursController {

    private final CoursService coursService;

    @GetMapping
    public ResponseEntity<List<CoursResponse>> getAll() {
        return ResponseEntity.ok(coursService.findAll().stream()
                .map(this::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoursResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(coursService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<CoursResponse> create(@RequestBody CoursRequest request) {
        Cours cours = new Cours();
        cours.setName(request.getName());
        cours.setDureeJours(request.getDureeJours());
        Cours saved = coursService.save(cours);

        if (request.getFormateurIds() != null && !request.getFormateurIds().isEmpty()) {
            saved = coursService.assignFormateurs(saved.getId(), request.getFormateurIds());
        }
        if (request.getPrerequisIds() != null) {
            saved = coursService.setPrerequis(saved.getId(), request.getPrerequisIds());
        }
        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoursResponse> update(@PathVariable Long id, @RequestBody CoursRequest request) {
        return ResponseEntity.ok(toResponse(coursService.updateNomEtDuree(id, request.getName(), request.getDureeJours())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        coursService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/formateurs")
    public ResponseEntity<CoursResponse> assignFormateurs(@PathVariable Long id,
                                                          @RequestBody List<Long> formateurIds) {
        return ResponseEntity.ok(toResponse(coursService.assignFormateurs(id, formateurIds)));
    }

    @PutMapping("/{id}/prerequis")
    public ResponseEntity<CoursResponse> setPrerequis(@PathVariable Long id,
                                                       @RequestBody List<Long> prerequisIds) {
        return ResponseEntity.ok(toResponse(coursService.setPrerequis(id, prerequisIds)));
    }

    private CoursResponse toResponse(Cours cours) {
        return toResponse(cours, new HashSet<>());
    }

    private CoursResponse toResponse(Cours cours, Set<Long> visited) {
        List<FormateurInfo> formateurs = cours.getFormateurs() != null
                ? cours.getFormateurs().stream()
                    .map(u -> new FormateurInfo(u.getUid(), u.getFirstName(), u.getLastName()))
                    .toList()
                : List.of();

        visited.add(cours.getId());

        List<CoursResponse> prerequis = cours.getPrerequis() != null
                ? cours.getPrerequis().stream()
                    .map(p -> visited.contains(p.getId())
                            ? new CoursResponse(p.getId(), p.getName(), p.getDureeJours(),
                                p.getFormateurs() != null
                                        ? p.getFormateurs().stream()
                                            .map(u -> new FormateurInfo(u.getUid(), u.getFirstName(), u.getLastName()))
                                            .toList()
                                        : List.of(),
                                List.of())
                            : toResponse(p, visited))
                    .toList()
                : List.of();

        return new CoursResponse(cours.getId(), cours.getName(), cours.getDureeJours(), formateurs, prerequis);
    }
}
