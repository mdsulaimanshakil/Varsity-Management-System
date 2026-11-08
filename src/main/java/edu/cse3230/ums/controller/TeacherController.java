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
@RequestMapping("/api/teachers")
public class TeacherController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public TeacherController(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    // READ ALL: GET /api/teachers
    @GetMapping
    public List<AppUser> getAll() {
        return repo.findByRole(Role.TEACHER);
    }

    // READ ONE: GET /api/teachers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        Optional<AppUser> teacher = repo.findByIdAndRole(id, Role.TEACHER);
        if (teacher.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "Teacher not found");
        }
        return ResponseEntity.ok(teacher.get());
    }

    // CREATE: POST /api/teachers
    @PostMapping
    public ResponseEntity<?> create(@RequestBody AppUser teacher) {
        if (isBlank(teacher.getUsername()) || isBlank(teacher.getPassword())) {
            return error(HttpStatus.BAD_REQUEST, "Username and password are required");
        }
        if (repo.existsByUsername(teacher.getUsername())) {
            return error(HttpStatus.CONFLICT, "Username already exists");
        }

        teacher.setId(null);
        teacher.setRole(Role.TEACHER);
        teacher.setPassword(encoder.encode(teacher.getPassword()));
        AppUser saved = repo.save(teacher);

        return ResponseEntity.created(URI.create("/api/teachers/" + saved.getId())).body(saved);
    }

    // UPDATE: PUT /api/teachers/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AppUser data) {
        Optional<AppUser> found = repo.findByIdAndRole(id, Role.TEACHER);
        if (found.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "Teacher not found");
        }
        AppUser teacher = found.get();

        if (!isBlank(data.getUsername()) && !data.getUsername().equals(teacher.getUsername())) {
            if (repo.existsByUsername(data.getUsername())) {
                return error(HttpStatus.CONFLICT, "Username already exists");
            }
            teacher.setUsername(data.getUsername());
        }
        teacher.setFullName(data.getFullName());
        teacher.setEmail(data.getEmail());
        teacher.setDepartment(data.getDepartment());

        if (!isBlank(data.getPassword())) {
            teacher.setPassword(encoder.encode(data.getPassword()));
        }

        return ResponseEntity.ok(repo.save(teacher));
    }

    // DELETE: DELETE /api/teachers/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<AppUser> teacher = repo.findByIdAndRole(id, Role.TEACHER);
        if (teacher.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "Teacher not found");
        }
        repo.delete(teacher.get());
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
