package br.project.portalapo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String role; // e.g., ADMIN, COORDENADOR, ALUNO

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private boolean mustChangePassword = true;

    @Column
    private String verificationCode;

    @Column
    private LocalDateTime verificationCodeExpiry;

    @ElementCollection
    @CollectionTable(name = "user_orientador_assignments", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "orientador_user_id")
    private List<Long> orientadorUserIds = new ArrayList<>();

    // Relacionamento com Pessoa se necessário
    // @OneToOne
    // @JoinColumn(name = "pessoa_id")
    // private Pessoa pessoa;

    public User() {}

    public User(Long id, String username, String password, String role, boolean ativo) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.ativo = ativo;
        this.mustChangePassword = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public LocalDateTime getVerificationCodeExpiry() {
        return verificationCodeExpiry;
    }

    public void setVerificationCodeExpiry(LocalDateTime verificationCodeExpiry) {
        this.verificationCodeExpiry = verificationCodeExpiry;
    }

    public List<Long> getOrientadorUserIds() {
        return orientadorUserIds;
    }

    public void setOrientadorUserIds(List<Long> orientadorUserIds) {
        this.orientadorUserIds = orientadorUserIds != null ? orientadorUserIds : new ArrayList<>();
    }

}
