package fr.eni.gestionformation.service;

import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {
    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    void findUserByUsername_UserFound() {
        User user = User.builder().email("admin@admin.com").build();
        when(userRepository.findByEmail("admin@admin.com")).thenReturn(Optional.of(user));
        UserDetails result = userDetailsServiceImpl.loadUserByUsername("admin@admin.com");
        assertThat(result.getUsername()).isEqualTo("admin@admin.com");
    }

    @Test
    void findUserByUsername_UserNotFound() {
        when(userRepository.findByEmail("nouser@email.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userDetailsServiceImpl.loadUserByUsername("nouser@email.com"));
    }
}
