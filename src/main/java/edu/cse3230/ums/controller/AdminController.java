package edu.cse3230.ums.controller;

import edu.cse3230.ums.entity.AppUser;
import edu.cse3230.ums.entity.Role;
import edu.cse3230.ums.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public AdminController(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    // READ ALL: GET /api/admins
    @GetMapping
    public List<AppUser> getAll() {
        return repo.findByRole(Role.ADMIN);
    }

    // READ ONE: GET /api/admins/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        Optional<AppUser> admin = repo.findByIdAndRole(id, Role.ADMIN);
        if (admin.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "Admin not found");
        }
        return ResponseEntity.ok(admin.get());
    }

    // CREATE: POST /api/admins
    @PostMapping
    public ResponseEntity<?> create(@RequestBody AppUser admin) {
        if (isBlank(admin.getUsername()) || isBlank(admin.getPassword())) {
            return error(HttpStatus.BAD_REQUEST, "Username and password are required");
        }
        if (repo.existsByUsername(admin.getUsername())) {
            return error(HttpStatus.CONFLICT, "Username already exists");
        }

        admin.setId(null);
        admin.setRole(Role.ADMIN);
        admin.setPassword(encoder.encode(admin.getPassword()));
        AppUser saved = repo.save(admin);

        return ResponseEntity.created(URI.create("/api/admins/" + saved.getId())).body(saved);
    }

    // UPDATE: PUT /api/admins/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AppUser data) {
        Optional<AppUser> found = repo.findByIdAndRole(id, Role.ADMIN);
        if (found.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "Admin not found");
        }
        AppUser admin = found.get();

        if (!isBlank(data.getUsername()) && !data.getUsername().equals(admin.getUsername())) {
            if (repo.existsByUsername(data.getUsername())) {
                return error(HttpStatus.CONFLICT, "Username already exists");
            }
            admin.setUsername(data.getUsername());
        }
        admin.setFullName(data.getFullName());
        admin.setEmail(data.getEmail());
        admin.setDepartment(data.getDepartment());


        if (!isBlank(data.getPassword())) {
            admin.setPassword(encoder.encode(data.getPassword()));
        }

        return ResponseEntity.ok(repo.save(admin));
    }

    // DELETE: DELETE /api/admins/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<AppUser> admin = repo.findByIdAndRole(id, Role.ADMIN);
        if (admin.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "Admin not found");
        }
        repo.delete(admin.get());
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
