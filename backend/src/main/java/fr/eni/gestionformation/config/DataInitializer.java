package fr.eni.gestionformation.config;

import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(@NonNull String @NonNull ... args) {
        if (userRepository.findByEmail("admin@admin.fr").isEmpty()) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("ADMINISTRATEUR")
                    .email("admin@admin.fr")
                    .password(passwordEncoder.encode("Admin123"))
                    .role(Role.ADMINISTRATEUR)
                    .build();
            userRepository.save(admin);
            System.out.println("Utilisateur admin créé : admin@admin.fr / Admin123");
        }
    }
}
