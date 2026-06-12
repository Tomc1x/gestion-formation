package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.FormateurInfo;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/formateurs")
@RequiredArgsConstructor
public class FormateurController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<FormateurInfo>> getAll() {
        List<FormateurInfo> formateurs = userRepository.findByRole(Role.FORMATEUR).stream()
                .map(u -> new FormateurInfo(u.getUid(), u.getFirstName(), u.getLastName()))
                .toList();
        return ResponseEntity.ok(formateurs);
    }
}
