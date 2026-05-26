package br.project.portalapo.model;

import br.project.portalapo.enums.StatusSolicitacao;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "solicitacoes")
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private String title;
    private String level;
    private String color;

    @Enumerated(EnumType.STRING)
    private StatusSolicitacao status;

    private LocalDate createdAt;
    private LocalDate updatedAt;
    private LocalDate completedAt;

    public Solicitacao() {}

    public Solicitacao(Long id, String category, String title, String level, String color,
                       StatusSolicitacao status, LocalDate createdAt, LocalDate updatedAt, LocalDate completedAt) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.level = level;
        this.color = color;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDate getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDate completedAt) {
        this.completedAt = completedAt;
    }
}
