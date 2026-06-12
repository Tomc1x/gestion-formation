package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.exception.UserNotFoundException;
import fr.eni.gestionformation.security.SecurityConfig;
import fr.eni.gestionformation.service.JwtService;
import fr.eni.gestionformation.service.UserAdminService;
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

@WebMvcTest(UserAdminController.class)
@Import(SecurityConfig.class)
class UserAdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserAdminService userAdminService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    private User adminUser() {
        return User.builder()
                .uid(1L)
                .email("admin@test.com")
                .password("hashed")
                .role(Role.ADMINISTRATEUR)
                .firstName("Admin")
                .lastName("ENI")
                .build();
    }

    @Test
    void findAll_AvecRoleAdministrateur_Retourne200() throws Exception {
        User u = User.builder().uid(2L).firstName("Jean").lastName("Dupont")
                .email("jean@test.com").role(Role.ETUDIANT).enabled(true).build();
        when(userAdminService.findAll()).thenReturn(List.of(u));

        mockMvc.perform(get("/api/admin/users")
                        .with(user(adminUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("jean@test.com"));
    }

    @Test
    void findById_idExistant_Retourne200() throws Exception {
        User u = User.builder().uid(2L).firstName("Jean").lastName("Dupont")
                .email("jean@test.com").role(Role.ETUDIANT).enabled(true).build();
        when(userAdminService.findById(2L)).thenReturn(u);

        mockMvc.perform(get("/api/admin/users/2")
                        .with(user(adminUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jean"));
    }

    @Test
    void findById_idInconnu_Retourne404() throws Exception {
        when(userAdminService.findById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/admin/users/99")
                        .with(user(adminUser())))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll_AvecRoleEtudiant_Retourne403() throws Exception {
        User etudiant = User.builder()
                .uid(2L).email("etudiant@test.com").password("hashed")
                .role(Role.ETUDIANT).firstName("Etu").lastName("Diant").build();

        mockMvc.perform(get("/api/admin/users")
                        .with(user(etudiant)))
                .andExpect(status().isForbidden());
    }
}
