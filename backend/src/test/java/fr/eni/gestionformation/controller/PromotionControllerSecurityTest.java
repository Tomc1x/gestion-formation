package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.entity.CoursPlanifie;
import fr.eni.gestionformation.entity.CoursPlanifieStatut;
import fr.eni.gestionformation.entity.Cours;
import fr.eni.gestionformation.entity.Promotion;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.security.SecurityConfig;
import fr.eni.gestionformation.service.JwtService;
import fr.eni.gestionformation.service.PromotionService;
import fr.eni.gestionformation.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Regression FULLST-019 : POST/DELETE /api/promotions/{id}/eleves/{eleveId} renvoyaient
 * 403 pour un utilisateur REFERENTE_ADMINISTRATIVE alors que SecurityConfig applique
 * hasRole("REFERENTE_ADMINISTRATIVE") sur /api/promotions/** (meme regle que pour
 * /api/cursus/** qui fonctionne). Verifie que ces routes restent accessibles pour
 * REFERENTE_ADMINISTRATIVE et refusees pour ETUDIANT.
 */
@WebMvcTest(PromotionController.class)
@Import(SecurityConfig.class)
class PromotionControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PromotionService promotionService;

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
    void addEleve_AvecRoleReferenteAdministrative_Retourne200() throws Exception {
        Promotion promotion = Promotion.builder().id(3L).name("Promo Test").build();
        when(promotionService.addEleve(3L, 13L)).thenReturn(promotion);
        when(promotionService.getEleves(3L)).thenReturn(List.of());
        when(promotionService.getPlanning(3L)).thenReturn(List.of());

        mockMvc.perform(post("/api/promotions/3/eleves/13")
                        .with(user(userWithRole(Role.REFERENTE_ADMINISTRATIVE))))
                .andExpect(status().isOk());
    }

    @Test
    void removeEleve_AvecRoleReferenteAdministrative_Retourne200() throws Exception {
        Promotion promotion = Promotion.builder().id(3L).name("Promo Test").build();
        when(promotionService.removeEleve(3L, 13L)).thenReturn(promotion);
        when(promotionService.getEleves(3L)).thenReturn(List.of());
        when(promotionService.getPlanning(3L)).thenReturn(List.of());

        mockMvc.perform(delete("/api/promotions/3/eleves/13")
                        .with(user(userWithRole(Role.REFERENTE_ADMINISTRATIVE))))
                .andExpect(status().isOk());
    }

    @Test
    void createPlanning_AvecRoleReferenteAdministrative_Retourne200() throws Exception {
        Cours cours = Cours.builder().id(5L).name("Cours A").build();
        CoursPlanifie coursPlanifie = CoursPlanifie.builder()
                .id(101L).cours(cours)
                .dateDebut(java.time.LocalDate.of(2026, 6, 22))
                .dateFin(java.time.LocalDate.of(2026, 6, 24))
                .ordre(2).statut(CoursPlanifieStatut.PLANIFIE).build();
        when(promotionService.createPlanning(org.mockito.ArgumentMatchers.eq(3L), any(), any())).thenReturn(coursPlanifie);

        mockMvc.perform(post("/api/promotions/3/planning")
                        .with(user(userWithRole(Role.REFERENTE_ADMINISTRATIVE)))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"coursId":5,"dateDebut":"2026-06-22","dateFin":"2026-06-24"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void createPlanning_AvecRoleEtudiant_Retourne403() throws Exception {
        mockMvc.perform(post("/api/promotions/3/planning")
                        .with(user(userWithRole(Role.ETUDIANT)))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"coursId":5,"dateDebut":"2026-06-22","dateFin":"2026-06-24"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void addEleve_AvecRoleEtudiant_Retourne403() throws Exception {
        mockMvc.perform(post("/api/promotions/3/eleves/13")
                        .with(user(userWithRole(Role.ETUDIANT))))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeEleve_AvecRoleEtudiant_Retourne403() throws Exception {
        mockMvc.perform(delete("/api/promotions/3/eleves/13")
                        .with(user(userWithRole(Role.ETUDIANT))))
                .andExpect(status().isForbidden());
    }
}
