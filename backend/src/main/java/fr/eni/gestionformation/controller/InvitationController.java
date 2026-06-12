package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.InvitationPreviewResponse;
import fr.eni.gestionformation.dto.InviteRequest;
import fr.eni.gestionformation.dto.RegisterWithTokenRequest;
import fr.eni.gestionformation.dto.UserAdminResponse;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    private UserAdminResponse toResponse(User user) {
        return new UserAdminResponse(
                user.getUid(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled()
        );
    }

    @GetMapping("/api/auth/invite/preview")
    public ResponseEntity<InvitationPreviewResponse> previewInvitation(@RequestParam String token) {
        return ResponseEntity.ok(invitationService.previewToken(token));
    }

    @PostMapping("/api/admin/invite")
    public ResponseEntity<String> sendInvitation(@RequestBody InviteRequest request) {
        String registrationLink = invitationService.sendInvitation(request.getEmail(), request.getRole());
        return ResponseEntity.ok(registrationLink);
    }

    @PostMapping("/api/auth/register-invitation")
    public ResponseEntity<UserAdminResponse> registerWithToken(@RequestBody RegisterWithTokenRequest request) {
        User created = invitationService.registerWithToken(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }
}
