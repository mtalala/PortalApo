package br.project.portalapo.model;

import jakarta.persistence.*;

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

    @Column(nullable = false)
    private String role; // e.g., ADMIN, COORDENADOR, ALUNO

    @Column(nullable = false)
    private boolean ativo = true;

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

}