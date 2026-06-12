package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.entity.Promotion;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.exception.PromotionNotFoundException;
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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests fonctionnels nominaux pour PromotionController. La sécurité/roles
 * est déjà couverte par PromotionControllerSecurityTest.
 */
@WebMvcTest(PromotionController.class)
@Import(SecurityConfig.class)
class PromotionControllerTest {

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
    void getAll_AvecRoleAdministrateur_Retourne200() throws Exception {
        Promotion promotion = Promotion.builder().id(1L).name("Promo A").build();
        when(promotionService.findAll()).thenReturn(List.of(promotion));
        when(promotionService.getEleves(1L)).thenReturn(List.of());
        when(promotionService.getPlanning(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/promotions")
                        .with(user(userWithRole(Role.ADMINISTRATEUR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Promo A"));
    }

    @Test
    void getById_idExistant_Retourne200() throws Exception {
        Promotion promotion = Promotion.builder().id(1L).name("Promo A").build();
        when(promotionService.findById(1L)).thenReturn(promotion);
        when(promotionService.getEleves(1L)).thenReturn(List.of());
        when(promotionService.getPlanning(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/promotions/1")
                        .with(user(userWithRole(Role.ADMINISTRATEUR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_idInconnu_Retourne404() throws Exception {
        when(promotionService.findById(99L)).thenThrow(new PromotionNotFoundException(99L));

        mockMvc.perform(get("/api/promotions/99")
                        .with(user(userWithRole(Role.ADMINISTRATEUR))))
                .andExpect(status().isNotFound());
    }
}
