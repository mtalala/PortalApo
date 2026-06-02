package br.project.portalapo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usuarioId;
    private String usuarioUsername;
    private String acao;
    private String entidade;
    private Long entidadeId;
    private String detalhe;
    private LocalDateTime timestamp;

    public AuditLog() {}

    public AuditLog(User user, String acao, String entidade, Long entidadeId, String detalhe) {
        this.usuarioId = user != null ? user.getId() : null;
        this.usuarioUsername = user != null ? user.getUsername() : null;
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.detalhe = detalhe;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getUsuarioUsername() {
        return usuarioUsername;
    }

    public String getAcao() {
        return acao;
    }

    public String getEntidade() {
        return entidade;
    }

    public Long getEntidadeId() {
        return entidadeId;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
