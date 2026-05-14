package com.pageturner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin", "/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/cart/**", "/orders/**", "/profile/**").authenticated()
                .requestMatchers("/**").permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    System.out.println(">>> AUTH SUCCESS HANDLER TRIGGERED FOR: " + authentication.getName());
                    System.out.println(">>> AUTHORITIES AT LOGIN: " + authentication.getAuthorities());
                    
                    boolean isAdmin = authentication.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    
                    System.out.println(">>> IS ADMIN EVALUATED AS: " + isAdmin);
                    
                    // Force explicit fallback session save
                    request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", 
                        org.springframework.security.core.context.SecurityContextHolder.getContext());
                        
                    if (isAdmin) {
                        System.out.println(">>> REDIRECTING TO /admin");
                        response.sendRedirect("/admin");
                    } else {
                        System.out.println(">>> REDIRECTING TO /");
                        response.sendRedirect("/");
                    }
                })
                .failureHandler((request, response, exception) -> {
                    System.out.println(">>> AUTH FAILURE TRIGGERED: " + exception.getMessage());
                    response.sendRedirect("/login?error=true");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());
        
        http.authenticationProvider(authenticationProvider());
        return http.build();
    }
}
