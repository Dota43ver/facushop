package com.facushop.repository;

import com.facushop.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Este método es CRUCIAL. Spring Security lo usará
    // para buscar al usuario por su "username" (nuestro email).
    Optional<User> findByEmail(String email);
}
