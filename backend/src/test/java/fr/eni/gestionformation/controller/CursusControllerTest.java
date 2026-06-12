package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.entity.Cursus;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.exception.CursusNotFoundException;
import fr.eni.gestionformation.security.SecurityConfig;
import fr.eni.gestionformation.service.CursusService;
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

@WebMvcTest(CursusController.class)
@Import(SecurityConfig.class)
class CursusControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CursusService cursusService;

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
        Cursus cursus = Cursus.builder().id(1L).name("Cursus Java").build();
        when(cursusService.findAll()).thenReturn(List.of(cursus));
        when(cursusService.getCoursOrdonnes(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/cursus")
                        .with(user(userWithRole(Role.ADMINISTRATEUR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cursus Java"));
    }

    @Test
    void getById_idExistant_Retourne200() throws Exception {
        Cursus cursus = Cursus.builder().id(1L).name("Cursus Java").build();
        when(cursusService.findById(1L)).thenReturn(cursus);
        when(cursusService.getCoursOrdonnes(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/cursus/1")
                        .with(user(userWithRole(Role.ADMINISTRATEUR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_idInconnu_Retourne404() throws Exception {
        when(cursusService.findById(99L)).thenThrow(new CursusNotFoundException(99L));

        mockMvc.perform(get("/api/cursus/99")
                        .with(user(userWithRole(Role.ADMINISTRATEUR))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_AvecRoleEtudiant_Retourne403() throws Exception {
        mockMvc.perform(get("/api/cursus")
                        .with(user(userWithRole(Role.ETUDIANT))))
                .andExpect(status().isForbidden());
    }
}
