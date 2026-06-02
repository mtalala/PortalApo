package br.project.portalapo.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String role;

    private String topic;

    @Column(name = "request_color")
    private String requestColor;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private boolean read = false;

    // =========================
    // CONSTRUCTORS
    // =========================

    public Notification() {
        this.createdAt = OffsetDateTime.now();
    }

    public Notification(String role, String topic, String requestColor) {
        this.role = role;
        this.topic = topic;
        this.requestColor = requestColor;
        this.createdAt = OffsetDateTime.now();
        this.read = false;
    }

    public Notification(
            String id,
            String role,
            String topic,
            String requestColor,
            OffsetDateTime createdAt,
            boolean read
    ) {
        this.id = id;
        this.role = role;
        this.topic = topic;
        this.requestColor = requestColor;
        this.createdAt = createdAt;
        this.read = read;
    }

    // =========================
    // GETTERS
    // =========================

    public String getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getTopic() {
        return topic;
    }

    public String getRequestColor() {
        return requestColor;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }

    // =========================
    // SETTERS
    // =========================

    public void setId(String id) {
        this.id = id;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setRequestColor(String requestColor) {
        this.requestColor = requestColor;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}