package br.com.fiap.semaforo.security;

import br.com.fiap.semaforo.dto.ErroResponse;
import br.com.fiap.semaforo.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {

    private static final String[] PUBLICOS = {
            "/auth/**", "/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
            "/actuator/health/**", "/actuator/info", "/h2-console/**"
    };

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtFilter, ObjectMapper mapper) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> { })
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLICOS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/cruzamentos/*/modo").permitAll()
                        .requestMatchers("/actuator/**", "/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/alertas/cemaden/sincronizar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/cruzamentos/*/leituras").hasAnyRole("OPERADOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/cruzamentos", "/alertas").hasAnyRole("OPERADOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/cruzamentos/*", "/alertas/*").hasAnyRole("OPERADOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/alertas/**").hasAnyRole("OPERADOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/cruzamentos/**", "/alertas/**").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                escrever(res, mapper, 401, "Unauthorized", "Token ausente, inválido ou expirado", req.getRequestURI()))
                        .accessDeniedHandler((req, res, ex) ->
                                escrever(res, mapper, 403, "Forbidden", "Seu papel não permite esta operação", req.getRequestURI())))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return email -> usuarios.findByEmailIgnoreCase(email)
                .map(u -> User.withUsername(u.getEmail()).password(u.getSenha()).roles(u.getPapel().name()).build())
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${cors.origins:*}") List<String> origens) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(origens);
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        c.setExposedHeaders(List.of("Location"));
        c.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", c);
        return src;
    }

    private static void escrever(HttpServletResponse res, ObjectMapper mapper, int status, String erro, String msg,
                                 String caminho) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getOutputStream(), new ErroResponse(status, erro, msg, caminho));
    }
}
