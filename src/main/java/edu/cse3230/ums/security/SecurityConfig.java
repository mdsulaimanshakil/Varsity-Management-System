package edu.cse3230.ums.security;

import edu.cse3230.ums.entity.AppUser;
import edu.cse3230.ums.entity.Role;
import edu.cse3230.ums.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository repo) {
        return username -> {
            AppUser user = repo.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            return User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // Public: the web pages
                        .requestMatchers("/", "/index.html", "/app.js", "/styles.css", "/favicon.svg").permitAll()

                        // Role-based access control
                        .requestMatchers("/api/admins/**").hasRole("ADMIN")
                        .requestMatchers("/api/teachers/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "TEACHER", "STUDENT")

                        // Everything else just needs a logged-in user
                        .anyRequest().authenticated()
                )

                // LOGIN: POST /api/auth/login with form fields "username" and "password"
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler((req, res, auth) -> sendJson(res, 200, "Login successful"))
                        .failureHandler((req, res, ex) -> sendJson(res, 401, "Invalid username or password"))
                        .permitAll()
                )


                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((req, res, auth) -> sendJson(res, 200, "Logged out"))
                )

                .httpBasic(basic -> basic
                        .authenticationEntryPoint((req, res, ex) -> sendJson(res, 401, "Please log in first")))

                // Not logged in -> 401, wrong role -> 403
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> sendJson(res, 401, "Please log in first"))
                        .accessDeniedHandler((req, res, e) -> sendJson(res, 403, "You do not have permission"))
                );

        return http.build();
    }


    @Bean
    public CommandLineRunner createDefaultUsers(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.count() > 0) {
                return;
            }
            repo.save(newUser("admin", "admin123", "System Administrator", "Administration", Role.ADMIN, encoder));
            repo.save(newUser("teacher", "teacher123", "Nabil Faiyaz Sadi", "CSE", Role.TEACHER, encoder));
            repo.save(newUser("student", "student123", "Alif Al Ahad", "CSE", Role.STUDENT, encoder));
            System.out.println("Default users created: admin / teacher / student");
        };
    }

    private AppUser newUser(String username, String password, String fullName, String department,
                            Role role, PasswordEncoder encoder) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setFullName(fullName);
        user.setEmail(username + "@university.edu");
        user.setDepartment(department);
        user.setRole(role);
        return user;
    }

    private static void sendJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }
}
