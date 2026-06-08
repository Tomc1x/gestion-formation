package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.FiliereRequest;
import fr.eni.gestionformation.dto.FiliereResponse;
import fr.eni.gestionformation.entity.Filiere;
import fr.eni.gestionformation.service.FiliereService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filiere")
@RequiredArgsConstructor
public class FiliereController {

    private final FiliereService filiereService;

    @PostMapping
    public ResponseEntity<FiliereResponse> save(@RequestBody FiliereRequest filiereRequest) {
        String name = filiereRequest.getName();
        Filiere filiere = new Filiere();
        filiere.setName(name);
        Filiere savedFiliere = filiereService.save(filiere);
        FiliereResponse response = new FiliereResponse(savedFiliere.getId(), savedFiliere.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FiliereResponse>> getAll() {
        return ResponseEntity.ok(filiereService.findAll().stream()
                .map(f -> new FiliereResponse(f.getId(), f.getName()))
                .toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<FiliereResponse>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(filiereService.findByName(name).stream()
                .map(f -> new FiliereResponse(f.getId(), f.getName()))
                .toList());
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByName(@RequestParam String name) {
        return ResponseEntity.ok(filiereService.existsByName(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FiliereResponse> getById(@PathVariable Long id) {
        Filiere filiere = filiereService.findById(id);
        FiliereResponse response = new FiliereResponse(filiere.getId(), filiere.getName());
        return ResponseEntity.ok(response);
    }
}
