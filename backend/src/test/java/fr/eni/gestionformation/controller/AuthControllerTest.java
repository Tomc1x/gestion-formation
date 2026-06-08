package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.repository.UserRepository;
import fr.eni.gestionformation.security.SecurityConfig;
import fr.eni.gestionformation.service.JwtService;
import fr.eni.gestionformation.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthenticationManager authenticationManager;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    @Test
    void login_AvecBonsIdentifiants_Retourne200() throws Exception {
        User user = User.builder()
                .email("admin@admin.fr")
                .password("hashed")
                .role(Role.ADMINISTRATEUR)
                .firstName("Admin")
                .lastName("ENI")
                .build();

        when(userRepository.findByEmail("admin@admin.fr")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@admin.fr\",\"password\":\"Admin1234!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.email").value("admin@admin.fr"));
    }

    @Test
    void login_AvecMauvaisMotDePasse_Retourne401() throws Exception {
        User user = User.builder()
                .email("test@test.com")
                .password("hashed")
                .role(Role.ETUDIANT)
                .firstName("Test")
                .lastName("User")
                .build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"WrongPassword!\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_SansToken_Retourne401() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"Test1234!\",\"firstName\":\"Test\",\"lastName\":\"User\",\"role\":\"ETUDIANT\"}"))
                .andExpect(status().isForbidden());
        }
}