package fr.eni.gestionformation.service;

import fr.eni.gestionformation.dto.InvitationPreviewResponse;
import fr.eni.gestionformation.entity.InvitationToken;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.exception.InvalidInvitationTokenException;
import fr.eni.gestionformation.exception.UserAlreadyExistsException;
import fr.eni.gestionformation.repository.InvitationTokenRepository;
import fr.eni.gestionformation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    InvitationTokenRepository tokenRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    InvitationService invitationService;

    @Test
    void sendInvitation_emailNonUtilise_creeTokenEtRetourneLien() {
        ReflectionTestUtils.setField(invitationService, "baseUrl", "http://localhost:4200/register");
        when(userRepository.findByEmail("nouveau@test.com")).thenReturn(Optional.empty());
        when(tokenRepository.findByEmailAndUsedFalse("nouveau@test.com")).thenReturn(Optional.empty());
        when(tokenRepository.save(any(InvitationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String lien = invitationService.sendInvitation("nouveau@test.com", Role.ETUDIANT);

        assertThat(lien).startsWith("http://localhost:4200/register?token=");
    }

    @Test
    void sendInvitation_emailDejaUtilise_throwsUserAlreadyExists() {
        when(userRepository.findByEmail("existant@test.com"))
                .thenReturn(Optional.of(User.builder().uid(1L).email("existant@test.com").build()));

        assertThrows(UserAlreadyExistsException.class,
                () -> invitationService.sendInvitation("existant@test.com", Role.ETUDIANT));
    }

    @Test
    void previewToken_tokenValide_retournePreview() {
        InvitationToken token = InvitationToken.builder()
                .token("abc123").email("invite@test.com").role(Role.ETUDIANT)
                .expirationDate(LocalDateTime.now().plusHours(1)).used(false).build();
        when(tokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));

        InvitationPreviewResponse response = invitationService.previewToken("abc123");

        assertThat(response.getEmail()).isEqualTo("invite@test.com");
        assertThat(response.getRole()).isEqualTo("ETUDIANT");
    }

    @Test
    void previewToken_tokenInconnu_throwsInvalidInvitationToken() {
        when(tokenRepository.findByToken("inconnu")).thenReturn(Optional.empty());

        assertThrows(InvalidInvitationTokenException.class, () -> invitationService.previewToken("inconnu"));
    }

    @Test
    void previewToken_tokenExpire_throwsInvalidInvitationToken() {
        InvitationToken token = InvitationToken.builder()
                .token("expire").email("invite@test.com").role(Role.ETUDIANT)
                .expirationDate(LocalDateTime.now().minusHours(1)).used(false).build();
        when(tokenRepository.findByToken("expire")).thenReturn(Optional.of(token));

        assertThrows(InvalidInvitationTokenException.class, () -> invitationService.previewToken("expire"));
    }
}
