package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.FiliereRequest;
import fr.eni.gestionformation.dto.FiliereResponse;
import fr.eni.gestionformation.mapper.FiliereMapper;
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
    private final FiliereMapper filiereMapper;

    @PostMapping
    public ResponseEntity<FiliereResponse> save(@RequestBody FiliereRequest filiereRequest) {
        var filiere = filiereMapper.toEntity(filiereRequest.getName());
        return ResponseEntity.ok(filiereMapper.toResponseDto(filiereService.save(filiere)));
    }

    @GetMapping
    public ResponseEntity<List<FiliereResponse>> getAll() {
        return ResponseEntity.ok(filiereMapper.toResponseDtoList(filiereService.findAll()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FiliereResponse>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(filiereMapper.toResponseDtoList(filiereService.findByName(name)));
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByName(@RequestParam String name) {
        return ResponseEntity.ok(filiereService.existsByName(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FiliereResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(filiereMapper.toResponseDto(filiereService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FiliereResponse> update(@PathVariable Long id, @RequestBody FiliereRequest filiereRequest) {
        return ResponseEntity.ok(filiereMapper.toResponseDto(filiereService.update(id, filiereRequest.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        filiereService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
