package fr.eni.gestionformation.service;

import fr.eni.gestionformation.dto.UserAdminCreateRequest;
import fr.eni.gestionformation.dto.UserAdminUpdateRequest;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.exception.UserAlreadyExistsException;
import fr.eni.gestionformation.exception.UserNotFoundException;
import fr.eni.gestionformation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserAdminService userAdminService;

    @Test
    void createUser_emailNonUtilise_creeUtilisateur() {
        UserAdminCreateRequest request = new UserAdminCreateRequest();
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setEmail("jean.dupont@test.com");
        request.setPassword("Password123!");
        request.setRole(Role.ETUDIANT);

        when(userRepository.findByEmail("jean.dupont@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userAdminService.createUser(request);

        assertThat(result.getEmail()).isEqualTo("jean.dupont@test.com");
        assertThat(result.getPassword()).isEqualTo("hashed");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void createUser_emailDejaUtilise_throwsUserAlreadyExists() {
        UserAdminCreateRequest request = new UserAdminCreateRequest();
        request.setEmail("existant@test.com");

        when(userRepository.findByEmail("existant@test.com"))
                .thenReturn(Optional.of(User.builder().uid(1L).email("existant@test.com").build()));

        assertThrows(UserAlreadyExistsException.class, () -> userAdminService.createUser(request));
    }

    @Test
    void findById_idInconnu_throwsUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userAdminService.findById(99L));
    }

    @Test
    void updateUser_emailValide_metAJourUtilisateur() {
        User user = User.builder().uid(1L).firstName("Ancien").lastName("Nom").email("ancien@test.com").build();
        UserAdminUpdateRequest request = new UserAdminUpdateRequest();
        request.setFirstName("Nouveau");
        request.setLastName("Nom");
        request.setEmail("ancien@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userAdminService.updateUser(1L, request);

        assertThat(result.getFirstName()).isEqualTo("Nouveau");
    }
}
