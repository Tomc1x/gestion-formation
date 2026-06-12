package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.EleveDisponibleInfo;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/eleves")
@RequiredArgsConstructor
public class EleveController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<EleveDisponibleInfo>> getAll() {
        List<EleveDisponibleInfo> eleves = userRepository.findByRole(Role.ETUDIANT).stream()
                .map(u -> new EleveDisponibleInfo(u.getUid(), u.getFirstName(), u.getLastName(), u.getEmail()))
                .toList();
        return ResponseEntity.ok(eleves);
    }
}
