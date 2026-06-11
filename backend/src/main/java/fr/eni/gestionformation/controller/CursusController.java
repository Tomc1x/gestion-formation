package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.AddCoursToCursusRequest;
import fr.eni.gestionformation.dto.CoursInCursusResponse;
import fr.eni.gestionformation.dto.CursusRequest;
import fr.eni.gestionformation.dto.CursusResponse;
import fr.eni.gestionformation.dto.FormateurInfo;
import fr.eni.gestionformation.dto.ReorderCoursRequest;
import fr.eni.gestionformation.entity.Cursus;
import fr.eni.gestionformation.entity.CursusCours;
import fr.eni.gestionformation.entity.Filiere;
import fr.eni.gestionformation.service.CursusService;
import fr.eni.gestionformation.service.FiliereService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursus")
@RequiredArgsConstructor
public class CursusController {

    private final CursusService cursusService;
    private final FiliereService filiereService;

    @GetMapping
    public ResponseEntity<List<CursusResponse>> getAll() {
        return ResponseEntity.ok(cursusService.findAll().stream()
                .map(this::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursusResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(cursusService.findById(id)));
    }

    @GetMapping("/filiere/{filiereId}")
    public ResponseEntity<List<CursusResponse>> getByFiliere(@PathVariable Long filiereId) {
        return ResponseEntity.ok(cursusService.findByFiliereId(filiereId).stream()
                .map(this::toResponse)
                .toList());
    }

    @PostMapping
    public ResponseEntity<CursusResponse> create(@RequestBody CursusRequest request) {
        Cursus cursus = new Cursus();
        cursus.setName(request.getName());
        if (request.getFiliereId() != null) {
            Filiere filiere = filiereService.findById(request.getFiliereId());
            cursus.setFiliere(filiere);
        }
        Cursus saved = cursusService.save(cursus);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cursusService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cours")
    public ResponseEntity<CursusResponse> addCours(@PathVariable Long id,
                                                    @RequestBody AddCoursToCursusRequest request) {
        cursusService.addCours(id, request.getCoursId(), request.getOrdre());
        return ResponseEntity.ok(toResponse(cursusService.findById(id)));
    }

    @DeleteMapping("/{id}/cours/{coursId}")
    public ResponseEntity<CursusResponse> removeCours(@PathVariable Long id, @PathVariable Long coursId) {
        cursusService.removeCours(id, coursId);
        return ResponseEntity.ok(toResponse(cursusService.findById(id)));
    }

    @PutMapping("/{id}/cours/reorder")
    public ResponseEntity<CursusResponse> reorderCours(@PathVariable Long id,
                                                        @RequestBody ReorderCoursRequest request) {
        cursusService.reorderCours(id, request.getCoursIds());
        return ResponseEntity.ok(toResponse(cursusService.findById(id)));
    }

    private CursusResponse toResponse(Cursus cursus) {
        Long filiereId = cursus.getFiliere() != null ? cursus.getFiliere().getId() : null;
        String filiereName = cursus.getFiliere() != null ? cursus.getFiliere().getName() : null;
        List<CoursInCursusResponse> cours = cursusService.getCoursOrdonnes(cursus.getId()).stream()
                .map(this::toCoursInCursusResponse)
                .toList();
        return new CursusResponse(cursus.getId(), cursus.getName(), filiereId, filiereName, cours);
    }

    private CoursInCursusResponse toCoursInCursusResponse(CursusCours cursusCours) {
        var c = cursusCours.getCours();
        List<FormateurInfo> formateurs = c.getFormateurs() != null
                ? c.getFormateurs().stream()
                    .map(u -> new FormateurInfo(u.getUid(), u.getFirstName(), u.getLastName()))
                    .toList()
                : List.of();
        return new CoursInCursusResponse(c.getId(), c.getName(), cursusCours.getOrdre(), formateurs);
    }
}
