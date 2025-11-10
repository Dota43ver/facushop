package com.facushop.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.List;

@Data // Combina @Getter, @Setter, @ToString, @EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user") // Usamos "_user" porque "user" suele ser una palabra reservada en SQL
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstname;
    private String lastname;

    @Column(unique = true) // El email será nuestro "username" y debe ser único
    private String email;

    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING) // Guarda el rol como "USER" o "ADMIN" en la BD
    private Role role;

    // --- Métodos de UserDetails ---
    // Estos métodos son requeridos por Spring Security

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Devolvemos una lista con el rol del usuario
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        // Nuestro "username" será el email
        return email;
    }

    @Override
    public String getPassword() {
        // Devolvemos la contraseña (ya hasheada)
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Asumimos que las cuentas no expiran
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Asumimos que no se bloquean
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Asumimos que las credenciales no expiran
    }

    @Override
    public boolean isEnabled() {
        return true; // Asumimos que las cuentas están habilitadas
    }
}
