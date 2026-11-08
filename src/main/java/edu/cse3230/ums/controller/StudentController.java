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
@RequestMapping("/api/students")
public class StudentController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public StudentController(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    // READ ALL: GET /api/students
    @GetMapping
    public List<AppUser> getAll() {
        return repo.findByRole(Role.STUDENT);
    }

    // READ ONE: GET /api/students/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        Optional<AppUser> student = repo.findByIdAndRole(id, Role.STUDENT);
        if (student.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "Student not found");
        }
        return ResponseEntity.ok(student.get());
    }

    // CREATE: POST /api/students
    @PostMapping
    public ResponseEntity<?> create(@RequestBody AppUser student) {
        if (isBlank(student.getUsername()) || isBlank(student.getPassword())) {
            return error(HttpStatus.BAD_REQUEST, "Username and password are required");
        }
        if (repo.existsByUsername(student.getUsername())) {
            return error(HttpStatus.CONFLICT, "Username already exists");
        }

        student.setId(null);
        student.setRole(Role.STUDENT);
        student.setPassword(encoder.encode(student.getPassword()));
        AppUser saved = repo.save(student);

        return ResponseEntity.created(URI.create("/api/students/" + saved.getId())).body(saved);
    }

    // UPDATE: PUT /api/students/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AppUser data) {
        Optional<AppUser> found = repo.findByIdAndRole(id, Role.STUDENT);
        if (found.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "Student not found");
        }
        AppUser student = found.get();

        if (!isBlank(data.getUsername()) && !data.getUsername().equals(student.getUsername())) {
            if (repo.existsByUsername(data.getUsername())) {
                return error(HttpStatus.CONFLICT, "Username already exists");
            }
            student.setUsername(data.getUsername());
        }
        student.setFullName(data.getFullName());
        student.setEmail(data.getEmail());
        student.setDepartment(data.getDepartment());

        if (!isBlank(data.getPassword())) {
            student.setPassword(encoder.encode(data.getPassword()));
        }

        return ResponseEntity.ok(repo.save(student));
    }

    // DELETE: DELETE /api/students/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<AppUser> student = repo.findByIdAndRole(id, Role.STUDENT);
        if (student.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "Student not found");
        }
        repo.delete(student.get());
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
