package edu.cse3230.ums.repository;

import edu.cse3230.ums.entity.AppUser;
import edu.cse3230.ums.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    List<AppUser> findByRole(Role role);

    Optional<AppUser> findByIdAndRole(Long id, Role role);

    boolean existsByUsername(String username);
}
