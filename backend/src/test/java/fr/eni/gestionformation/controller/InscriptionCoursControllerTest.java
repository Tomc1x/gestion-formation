package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.entity.InscriptionCours;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.exception.CoursPlanifieNotFoundException;
import fr.eni.gestionformation.entity.CoursPlanifie;
import fr.eni.gestionformation.security.SecurityConfig;
import fr.eni.gestionformation.service.InscriptionCoursService;
import fr.eni.gestionformation.service.JwtService;
import fr.eni.gestionformation.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InscriptionCoursController.class)
@Import(SecurityConfig.class)
class InscriptionCoursControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    InscriptionCoursService inscriptionCoursService;

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
    void creerInscription_AvecRoleReferenteAdministrative_Retourne201() throws Exception {
        User eleve = User.builder().uid(5L).firstName("Jean").lastName("Dupont").build();
        CoursPlanifie coursPlanifie = CoursPlanifie.builder().id(100L).build();
        InscriptionCours inscription = InscriptionCours.builder()
                .id(1L).eleve(eleve).coursPlanifie(coursPlanifie)
                .dateInscription(LocalDate.now()).build();
        when(inscriptionCoursService.creerInscription(100L, 5L)).thenReturn(inscription);

        mockMvc.perform(post("/api/cours-planifies/100/inscriptions")
                        .with(user(userWithRole(Role.REFERENTE_ADMINISTRATIVE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eleveId":5}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void creerInscription_coursPlanifieInconnu_Retourne404() throws Exception {
        when(inscriptionCoursService.creerInscription(99L, 5L))
                .thenThrow(new CoursPlanifieNotFoundException(99L));

        mockMvc.perform(post("/api/cours-planifies/99/inscriptions")
                        .with(user(userWithRole(Role.REFERENTE_ADMINISTRATIVE)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eleveId":5}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void creerInscription_AvecRoleEtudiant_Retourne403() throws Exception {
        mockMvc.perform(post("/api/cours-planifies/100/inscriptions")
                        .with(user(userWithRole(Role.ETUDIANT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eleveId":5}
                                """))
                .andExpect(status().isForbidden());
    }
}
