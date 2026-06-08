package fr.eni.gestionformation.controller;

import fr.eni.gestionformation.dto.UserAdminChangePasswordRequest;
import fr.eni.gestionformation.dto.UserAdminCreateRequest;
import fr.eni.gestionformation.dto.UserAdminResponse;
import fr.eni.gestionformation.dto.UserAdminUpdateRequest;
import fr.eni.gestionformation.dto.UserAdminUpdateRoleRequest;
import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

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

    @GetMapping
    public ResponseEntity<List<UserAdminResponse>> findAll() {
        List<UserAdminResponse> users = userAdminService.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAdminResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(userAdminService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<UserAdminResponse> createUser(@RequestBody UserAdminCreateRequest request) {
        User created = userAdminService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserAdminResponse> updateUser(@PathVariable Long id,
                                                        @RequestBody UserAdminUpdateRequest request) {
        return ResponseEntity.ok(toResponse(userAdminService.updateUser(id, request)));
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<UserAdminResponse> enableUser(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(userAdminService.enableUser(id)));
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<UserAdminResponse> disableUser(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(userAdminService.disableUser(id)));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<UserAdminResponse> changePassword(@PathVariable Long id,
                                                            @RequestBody UserAdminChangePasswordRequest request) {
        return ResponseEntity.ok(toResponse(userAdminService.changePassword(id, request.getNewPassword())));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserAdminResponse> changeRole(@PathVariable Long id,
                                                        @RequestBody UserAdminUpdateRoleRequest request) {
        return ResponseEntity.ok(toResponse(userAdminService.changeRole(id, request.getRole())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userAdminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
