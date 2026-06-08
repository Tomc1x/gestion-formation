package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.CoursRequest;
import fr.eni.gestionformation.dto.CoursResponse;
import fr.eni.gestionformation.dto.FormateurInfo;
import fr.eni.gestionformation.entity.Cours;
import fr.eni.gestionformation.entity.Cursus;
import fr.eni.gestionformation.service.CoursService;
import fr.eni.gestionformation.service.CursusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
@RequiredArgsConstructor
public class CoursController {

    private final CoursService coursService;
    private final CursusService cursusService;

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

    @GetMapping("/cursus/{cursusId}")
    public ResponseEntity<List<CoursResponse>> getByCursus(@PathVariable Long cursusId) {
        return ResponseEntity.ok(coursService.findByCursusId(cursusId).stream()
                .map(this::toResponse)
                .toList());
    }

    @PostMapping
    public ResponseEntity<CoursResponse> create(@RequestBody CoursRequest request) {
        Cours cours = new Cours();
        cours.setName(request.getName());
        if (request.getCursusId() != null) {
            Cursus cursus = cursusService.findById(request.getCursusId());
            cours.setCursus(cursus);
        }
        if (request.getFormateurIds() != null && !request.getFormateurIds().isEmpty()) {
            Cours saved = coursService.save(cours);
            Cours withFormateurs = coursService.assignFormateurs(saved.getId(), request.getFormateurIds());
            return ResponseEntity.ok(toResponse(withFormateurs));
        }
        return ResponseEntity.ok(toResponse(coursService.save(cours)));
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

    private CoursResponse toResponse(Cours cours) {
        Long cursusId = cours.getCursus() != null ? cours.getCursus().getId() : null;
        String cursusName = cours.getCursus() != null ? cours.getCursus().getName() : null;
        List<FormateurInfo> formateurs = cours.getFormateurs() != null
                ? cours.getFormateurs().stream()
                    .map(u -> new FormateurInfo(u.getUid(), u.getFirstName(), u.getLastName()))
                    .toList()
                : List.of();
        return new CoursResponse(cours.getId(), cours.getName(), cursusId, cursusName, formateurs);
    }
}
