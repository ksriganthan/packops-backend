package ch.packops.packopsbackend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/ping").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("admin")
                .requestMatchers(HttpMethod.POST, "/api/users").hasRole("admin")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("admin")

                .requestMatchers(HttpMethod.GET, "/api/users/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/**").authenticated()

                .requestMatchers(HttpMethod.GET, "/api/products/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("admin")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("admin")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("admin")

                .requestMatchers(HttpMethod.GET, "/api/configuration").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/configuration").hasAnyRole("admin", "operator")

                .requestMatchers(HttpMethod.GET, "/api/process/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/process/start").hasAnyRole("admin", "operator")
                .requestMatchers(HttpMethod.POST, "/api/process/*/stop").hasAnyRole("admin", "operator")

                .requestMatchers(HttpMethod.GET, "/api/statistics/**").authenticated()

                .requestMatchers("/api/auth/logout").authenticated()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
        )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // Wir speichern Rollen im JWT Claim "role"
        authoritiesConverter.setAuthoritiesClaimName("role");

        // Spring Security erwartet standardmässig ROLE_admin / ROLE_operator / ROLE_viewer
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}