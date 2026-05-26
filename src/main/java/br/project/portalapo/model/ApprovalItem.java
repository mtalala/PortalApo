package br.project.portalapo.model;

import br.project.portalapo.enums.RoleAprovacao;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class ApprovalItem {

    private String userId;

    @Enumerated(EnumType.STRING)
    private RoleAprovacao role;

    private boolean approved;

    public ApprovalItem() {}

    public ApprovalItem(String userId, RoleAprovacao role, boolean approved) {
        this.userId = userId;
        this.role = role;
        this.approved = approved;
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
}