package com.webapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
            .csrf(csrf -> csrf.disable())
            .authorizeRequests((authz) ->authz
                    .requestMatchers("/healthz").permitAll()
                    .requestMatchers(HttpMethod.POST,"/api/users").permitAll()
                    .requestMatchers(HttpMethod.GET,"/api/users/{id}").permitAll()
                    .requestMatchers(HttpMethod.PUT,"/api/users").permitAll()
                    .requestMatchers(HttpMethod.DELETE,"/api/users/{id}").permitAll()
                    .anyRequest().permitAll()
            )
            .httpBasic(withDefaults());
        return http.build();
    }
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests((authz) -> authz
//                        .requestMatchers("/healthz").permitAll()
//                        .requestMatchers(HttpMethod.POST,"/api/users").permitAll()
//                        .requestMatchers(HttpMethod.GET,"/api/users/{id}").permitAll()
//                        .requestMatchers(HttpMethod.PUT,"/api/users").permitAll()
//                        .requestMatchers(HttpMethod.DELETE,"/api/users/{id}").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .httpBasic(withDefaults());
//        return http.build();
//    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }



}






