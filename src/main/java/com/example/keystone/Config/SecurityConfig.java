package com.example.keystone.Config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
            .requestMatchers("/users/**").hasRole("MANAGER")
            .requestMatchers("/customers/**", "/sites/**").hasAnyRole("MANAGER", "DISPATCHER")
            .requestMatchers("/work-orders/new", "/work-orders/new/**").hasAnyRole("DISPATCHER", "MANAGER")
            .requestMatchers("/work-orders/request", "/work-orders/request/**").hasRole("CUSTOMER")
            .anyRequest().authenticated())
          .formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
            .usernameParameter("email").passwordParameter("password")
            .defaultSuccessUrl("/dashboard", true).failureUrl("/login?error=true").permitAll())
          .logout(logout -> logout.logoutSuccessUrl("/login?logout=true").permitAll());
        return http.build();
    }
}
