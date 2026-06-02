package br.project.portalapo.model;

import br.project.portalapo.enums.RoleAprovacao;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

@Embeddable
public class ApprovalItem {

    private String userId;

    @Enumerated(EnumType.STRING)
    private RoleAprovacao role;

    private boolean approved;
    private String justificativa;
    private LocalDateTime avaliadoEm;

    public ApprovalItem() {}

    public ApprovalItem(String userId, RoleAprovacao role, boolean approved) {
        this(userId, role, approved, null);
    }

    public ApprovalItem(String userId, RoleAprovacao role, boolean approved, String justificativa) {
        this.userId = userId;
        this.role = role;
        this.approved = approved;
        this.justificativa = justificativa;
        this.avaliadoEm = LocalDateTime.now();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public RoleAprovacao getRole() {
        return role;
    }

    public void setRole(RoleAprovacao role) {
        this.role = role;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public LocalDateTime getAvaliadoEm() {
        return avaliadoEm;
    }

    public void setAvaliadoEm(LocalDateTime avaliadoEm) {
        this.avaliadoEm = avaliadoEm;
    }
}
