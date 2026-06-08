package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.CursusRequest;
import fr.eni.gestionformation.dto.CursusResponse;
import fr.eni.gestionformation.entity.Cursus;
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
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestParam(defaultValue = "false") boolean cascade) {
        cursusService.deleteById(id, cascade);
        return ResponseEntity.noContent().build();
    }

    private CursusResponse toResponse(Cursus cursus) {
        Long filiereId = cursus.getFiliere() != null ? cursus.getFiliere().getId() : null;
        String filiereName = cursus.getFiliere() != null ? cursus.getFiliere().getName() : null;
        return new CursusResponse(cursus.getId(), cursus.getName(), filiereId, filiereName);
    }
}
