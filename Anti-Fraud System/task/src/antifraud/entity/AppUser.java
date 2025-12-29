package antifraud.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
public class AppUser {
    @Id
    @GeneratedValue
    private Long id;
    @NotNull(message = "Name may not be null")
    private String name;
    @NotNull(message = "Username may not be null")
    @Column(unique = true)
    private String username;
    @NotNull(message = "Password may not be null")
    @JsonIgnore
    private String password;
    @NotEmpty(message = "Role may not be empty")
    private String role;
    @JsonIgnore
    private boolean isLocked;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthority() {
        return role;
    }

    public void setAuthority(String role) {
        this.role = role;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}
