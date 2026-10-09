package com.quickbite.backend.config;

import com.quickbite.backend.security.CustomUserDetailsService;
import com.quickbite.backend.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) ->
                    writeError(res, 401, "Authentication required. Please log in.", req.getRequestURI()))
                .accessDeniedHandler((req, res, e) ->
                    writeError(res, 403, "You do not have permission to perform this action.", req.getRequestURI())))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()

                // Public browsing: GET only (dish search is GET /api/foods)
                .requestMatchers(HttpMethod.GET, "/api/restaurants/mine").hasRole("RESTAURANT_OWNER")
                .requestMatchers(HttpMethod.GET, "/api/restaurants/**", "/api/categories/**", "/api/foods").permitAll()

                // Admin
                .requestMatchers("/api/admin/**", "/api/categories/**").hasRole("ADMIN")

                // Owner: any non-GET on restaurants/foods, owner order endpoints, payment marking
                .requestMatchers("/api/restaurants/**", "/api/foods/**").hasRole("RESTAURANT_OWNER")
                .requestMatchers("/api/restaurant/**").hasRole("RESTAURANT_OWNER")
                .requestMatchers(HttpMethod.PUT, "/api/payments/*/mark-paid", "/api/payments/*/mark-failed")
                    .hasRole("RESTAURANT_OWNER")

                // Customer only
                .requestMatchers("/api/cart/**", "/api/addresses/**", "/api/coupons/validate").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.GET, "/api/orders").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/orders", "/api/reviews").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.PUT, "/api/orders/*/cancel").hasRole("CUSTOMER")

                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static void writeError(HttpServletResponse res, int status, String message, String path) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        String safePath = path == null ? "" : path.replace("\"", "");
        res.getWriter().write(String.format(
            "{\"timestamp\":\"%s\",\"status\":%d,\"message\":\"%s\",\"path\":\"%s\"}",
            LocalDateTime.now(), status, message, safePath));
    }
}