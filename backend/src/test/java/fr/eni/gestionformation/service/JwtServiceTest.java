package fr.eni.gestionformation.service;

import fr.eni.gestionformation.entity.User;
import fr.eni.gestionformation.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "XxLFGFrP&CghtQ8!cSFdFMHV%akaVk$^Upr!PbiaSBnvP%&8*3kUOz0#dhAo@y*qcG^ZehHeTuaPyiBXZdiyac7dAu61s^EhQ@oN*cS$baOPolzjqlCMXa@EzjOmgf0l");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        User user = User.builder().email("test@test.fr").role(Role.ETUDIANT).build();
        String token = jwtService.generateToken(user);
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void extractEmail_ShouldReturnCorrectEmail(){
        User user = User.builder().email("test@email.fr").role(Role.ETUDIANT).build();
        String token = jwtService.generateToken(user);
        String email = jwtService.extractEmail(token);
        assertEquals("test@email.fr", email);
    }

    @Test
    void isTokenValid_WithWrongUser_ShouldReturnFalse(){
        User user1 = User.builder().email("test@test.fr").role(Role.ETUDIANT).build();
        User user2 = User.builder().email("test@other.fr").role(Role.ETUDIANT).build();
        String token = jwtService.generateToken(user1);
        assertTrue(jwtService.isTokenValid(token, user1));
        assertFalse(jwtService.isTokenValid(token, user2));
    }
}
