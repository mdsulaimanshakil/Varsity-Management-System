package edu.cse3230.ums.controller;

import edu.cse3230.ums.entity.AppUser;
import edu.cse3230.ums.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final UserRepository repo;

    public AuthController(UserRepository repo) {
        this.repo = repo;
    }

    // GET /api/auth/me
    @GetMapping("/api/auth/me")
    public ResponseEntity<AppUser> me(Authentication auth) {
        return ResponseEntity.ok(repo.findByUsername(auth.getName()).orElseThrow());
    }
}
