package fr.eni.gestionformation.service;

import fr.eni.gestionformation.dto.InvitationPreviewResponse;
import fr.eni.gestionformation.dto.RegisterWithTokenRequest;
import fr.eni.gestionformation.entity.InvitationToken;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import fr.eni.gestionformation.exception.InvalidInvitationTokenException;
import fr.eni.gestionformation.exception.UserAlreadyExistsException;
import fr.eni.gestionformation.repository.InvitationTokenRepository;
import fr.eni.gestionformation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.invitation.base-url}")
    private String baseUrl;

    @Transactional
    public String sendInvitation(String email, Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException(email);
        }

        tokenRepository.findByEmailAndUsedFalse(email).ifPresent(existing -> {
            existing.setUsed(true);
            tokenRepository.save(existing);
        });

        String tokenValue = UUID.randomUUID().toString();
        String registrationLink = baseUrl + "?token=" + tokenValue;

        InvitationToken invitationToken = InvitationToken.builder()
                .token(tokenValue)
                .email(email)
                .role(role)
                .expirationDate(LocalDateTime.now().plusHours(24))
                .build();
        tokenRepository.save(invitationToken);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@gestion-formation.fr");
        message.setTo(email);
        message.setSubject("Invitation à rejoindre Gestion Formation");
        message.setText("Vous avez été invité(e). Cliquez sur ce lien pour finaliser votre inscription : "
                + registrationLink);

        try {
            mailSender.send(message);
            log.info("Invitation envoyée par e-mail à {}", email);
        } catch (MailException ex) {
            log.warn("Impossible d'envoyer l'e-mail d'invitation à {} : {}", email, ex.getMessage());
            log.info("=== LIEN D'INSCRIPTION (DEV) ===");
            log.info(registrationLink);
            log.info("================================");
        }

        return registrationLink;
    }

    public InvitationPreviewResponse previewToken(String tokenValue) {
        InvitationToken invitationToken = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidInvitationTokenException("Token d'invitation invalide."));
        if (invitationToken.isUsed()) {
            throw new InvalidInvitationTokenException("Ce lien a déjà été utilisé.");
        }
        if (!invitationToken.getExpirationDate().isAfter(LocalDateTime.now())) {
            throw new InvalidInvitationTokenException("Ce lien d'invitation a expiré.");
        }
        return new InvitationPreviewResponse(invitationToken.getEmail(), invitationToken.getRole().name());
    }

    public User registerWithToken(RegisterWithTokenRequest req) {
        InvitationToken invitationToken = tokenRepository.findByToken(req.getToken())
                .orElseThrow(() -> new InvalidInvitationTokenException("Token d'invitation invalide."));

        if (invitationToken.isUsed()) {
            throw new InvalidInvitationTokenException("Token déjà utilisé.");
        }

        if (!invitationToken.getExpirationDate().isAfter(LocalDateTime.now())) {
            throw new InvalidInvitationTokenException("Token expiré.");
        }

        if (userRepository.findByEmail(invitationToken.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(invitationToken.getEmail());
        }

        User user = User.builder()
                .email(invitationToken.getEmail())
                .role(invitationToken.getRole())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .enabled(true)
                .build();
        userRepository.save(user);

        invitationToken.setUsed(true);
        tokenRepository.save(invitationToken);

        return user;
    }
}
