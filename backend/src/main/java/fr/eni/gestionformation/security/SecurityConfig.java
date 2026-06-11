package fr.eni.gestionformation.security;

import fr.eni.gestionformation.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register").hasAnyRole("ADMINISTRATEUR", "REFERENTE_ADMINISTRATIVE")
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/filiere/**").hasAnyRole("ADMINISTRATEUR", "REFERENTE_ADMINISTRATIVE")
                .requestMatchers("/api/filiere/**").hasRole("REFERENTE_ADMINISTRATIVE")
                .requestMatchers(HttpMethod.GET, "/api/cursus/**").hasAnyRole("ADMINISTRATEUR", "REFERENTE_ADMINISTRATIVE")
                .requestMatchers("/api/cursus/**").hasRole("REFERENTE_ADMINISTRATIVE")
                .requestMatchers(HttpMethod.GET, "/api/cours/**").hasAnyRole("ADMINISTRATEUR", "REFERENTE_ADMINISTRATIVE")
                .requestMatchers("/api/cours/**").hasRole("REFERENTE_ADMINISTRATIVE")
                .requestMatchers(HttpMethod.GET, "/api/promotions/**").hasAnyRole("ADMINISTRATEUR", "REFERENTE_ADMINISTRATIVE")
                .requestMatchers("/api/promotions/**").hasRole("REFERENTE_ADMINISTRATIVE")
                .requestMatchers(HttpMethod.GET, "/api/cours-planifies/*/inscrits").hasAnyRole("ADMINISTRATEUR", "REFERENTE_ADMINISTRATIVE", "FORMATEUR")
                .requestMatchers("/api/cours-planifies/**").hasAnyRole("ADMINISTRATEUR", "REFERENTE_ADMINISTRATIVE")
                .requestMatchers(HttpMethod.GET, "/api/eleves/*/planning").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/admin/users").hasRole("ADMINISTRATEUR")
                .requestMatchers("/api/admin/**").hasRole("ADMINISTRATEUR")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}
