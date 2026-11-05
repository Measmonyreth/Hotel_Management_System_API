package com.veth007.hotelmanagement.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.veth007.hotelmanagement.config.Handle.CookieOAuth2AuthorizationRequestRepository;
import com.veth007.hotelmanagement.config.Handle.OAuth2SuccessHandler;
import com.veth007.hotelmanagement.service.CustomerDetailsService;
import com.veth007.hotelmanagement.service.EmployeeDetailsService;
import com.veth007.hotelmanagement.service.OAuth2.OAuth2UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        // ── 1. PasswordEncoder ────────────────────────────────
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public DaoAuthenticationProvider employeeAuthProvider(EmployeeDetailsService employeeDetailsService,
                        PasswordEncoder passwordEncoder) {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(employeeDetailsService);
                provider.setPasswordEncoder(passwordEncoder);
                return provider;
        }

        @Bean
        public DaoAuthenticationProvider customerAuthProvider(CustomerDetailsService customerDetailsService,
                        PasswordEncoder passwordEncoder) {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customerDetailsService);
                provider.setPasswordEncoder(passwordEncoder);
                return provider;
        }

        // ── 2. AuthenticationManager ──────────────────────────
        @Bean
        public AuthenticationManager authenticationManager(
                        DaoAuthenticationProvider employeeAuthProvider,
                        DaoAuthenticationProvider customerAuthProvider) {
                return new ProviderManager(employeeAuthProvider, customerAuthProvider);
        }

        // ── Cookie-based OAuth2 authorization request repo ──
        @Bean
        public CookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository() {
                return new CookieOAuth2AuthorizationRequestRepository();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(java.util.List.of("*"));
                configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Accept", "Origin",
                                "X-API-KEY"));
                configuration.setExposedHeaders(java.util.List.of("Authorization"));
                configuration.setAllowCredentials(false);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        // ── 3. SecurityFilterChain ────────────────────────────
        @Bean
        public SecurityFilterChain filterChain(
                        HttpSecurity http,
                        JwtAuthFilter jwtAuthFilter,
                        ApiKeyFilter apiKeyFilter,
                        OAuth2UserService oauth2UserService,
                        OAuth2SuccessHandler oAuth2SuccessHandler,
                        ObjectProvider<ClientRegistrationRepository> clientRegistrations

        ) throws Exception {

                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                // ── Static Resources & Favicon ──────────

                                                // // ── Public API Routes ───────────────────
                                                // .requestMatchers("/api/auth/**").permitAll()
                                                // .requestMatchers("/api/developer/**").permitAll()
                                                // .requestMatchers("/api/email/**").permitAll()
                                                .requestMatchers("/api/developer/**").permitAll()

                                                // ── Allow public access to application ───────────────

                                                .requestMatchers("/api/auth/login",
                                                                "/api/auth/register")
                                                .permitAll()

                                                .requestMatchers("/api/**").permitAll() // for testing only, consider
                                                                                        // removing later
                                                // .requestMatchers("/api/book/**").permitAll()
                                                // .requestMatchers("/api/customers/**").permitAll()

                                                .requestMatchers("/api/hotel_management/room/*/images/**").permitAll()
                                                .requestMatchers("/api/hotle_managemnet/room*/images/**").permitAll() // old
                                                                                                                      // typo
                                                .requestMatchers("/favicon.ico", "/favicon.png", "/favicon.jpg")
                                                .permitAll()
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**")
                                                .permitAll() // path
                                                .requestMatchers(
                                                                "/oauth2/**",
                                                                "/login/oauth2/**")
                                                .permitAll()
                                                .requestMatchers(
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs",
                                                                "/v3/api-docs/**",
                                                                "/api-docs/**",
                                                                "/api-docs.yaml")
                                                .permitAll()

                                                // for paypal payment
                                                .requestMatchers("/paypal/**").permitAll()

                                                // // ── Role protected ───────────────────────
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/manager/**").hasAnyRole("ADMIN", "MANAGER")
                                                .requestMatchers("/api/employee/**")
                                                .hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                                                // ── Everything else needs login ──────────
                                                .anyRequest().authenticated())
                                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterAfter(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                ;

                if (clientRegistrations.getIfAvailable() != null) {
                        http.oauth2Login(oauth2 -> oauth2
                                        .authorizationEndpoint(auth -> auth
                                                        .baseUri("/oauth2/authorize")
                                                        .authorizationRequestRepository(
                                                                        cookieOAuth2AuthorizationRequestRepository()))
                                        .redirectionEndpoint(redirection -> redirection
                                                        .baseUri("/login/oauth2/code/*"))
                                        .userInfoEndpoint(ui -> ui.userService(oauth2UserService))
                                        .successHandler(oAuth2SuccessHandler));
                }

                return http.build();

        }
}
