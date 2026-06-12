package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.InvitationPreviewResponse;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.exception.InvalidInvitationTokenException;
import fr.eni.gestionformation.security.SecurityConfig;
import fr.eni.gestionformation.service.InvitationService;
import fr.eni.gestionformation.service.JwtService;
import fr.eni.gestionformation.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvitationController.class)
@Import(SecurityConfig.class)
class InvitationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    InvitationService invitationService;

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
    void previewInvitation_tokenValide_Retourne200() throws Exception {
        when(invitationService.previewToken("abc123"))
                .thenReturn(new InvitationPreviewResponse("invite@test.com", "ETUDIANT"));

        mockMvc.perform(get("/api/auth/invite/preview").param("token", "abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("invite@test.com"));
    }

    @Test
    void previewInvitation_tokenInvalide_Retourne400() throws Exception {
        when(invitationService.previewToken("invalide"))
                .thenThrow(new InvalidInvitationTokenException("Token d'invitation invalide."));

        mockMvc.perform(get("/api/auth/invite/preview").param("token", "invalide"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendInvitation_AvecRoleAdministrateur_Retourne200() throws Exception {
        when(invitationService.sendInvitation("nouveau@test.com", Role.ETUDIANT))
                .thenReturn("http://localhost:4200/register?token=abc123");

        mockMvc.perform(post("/api/admin/invite")
                        .with(user(adminUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nouveau@test.com","role":"ETUDIANT"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void sendInvitation_AvecRoleEtudiant_Retourne403() throws Exception {
        User etudiant = User.builder()
                .uid(2L).email("etudiant@test.com").password("hashed")
                .role(Role.ETUDIANT).firstName("Etu").lastName("Diant").build();

        mockMvc.perform(post("/api/admin/invite")
                        .with(user(etudiant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nouveau@test.com","role":"ETUDIANT"}
                                """))
                .andExpect(status().isForbidden());
    }
}
