package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.entity.Filiere;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.exception.FiliereNotFoundException;
import fr.eni.gestionformation.security.SecurityConfig;
import fr.eni.gestionformation.service.FiliereService;
import fr.eni.gestionformation.service.JwtService;
import fr.eni.gestionformation.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FiliereController.class)
@Import(SecurityConfig.class)
class FiliereControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FiliereService filiereService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    private User userWithRole(Role role) {
        return User.builder()
                .uid(1L)
                .email("user@test.com")
                .password("hashed")
                .role(role)
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void getAll_AvecRoleAdministrateur_Retourne200() throws Exception {
        when(filiereService.findAll()).thenReturn(List.of(Filiere.builder().id(1L).name("Developpement").build()));

        mockMvc.perform(get("/api/filiere")
                        .with(user(userWithRole(Role.ADMINISTRATEUR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Developpement"));
    }

    @Test
    void getById_idExistant_Retourne200() throws Exception {
        when(filiereService.findById(1L)).thenReturn(Filiere.builder().id(1L).name("Developpement").build());

        mockMvc.perform(get("/api/filiere/1")
                        .with(user(userWithRole(Role.ADMINISTRATEUR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_idInconnu_Retourne404() throws Exception {
        when(filiereService.findById(99L)).thenThrow(new FiliereNotFoundException(99L));

        mockMvc.perform(get("/api/filiere/99")
                        .with(user(userWithRole(Role.ADMINISTRATEUR))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_AvecRoleEtudiant_Retourne403() throws Exception {
        mockMvc.perform(get("/api/filiere")
                        .with(user(userWithRole(Role.ETUDIANT))))
                .andExpect(status().isForbidden());
    }
}
