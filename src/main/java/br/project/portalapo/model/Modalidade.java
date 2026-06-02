package br.project.portalapo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "modalidades")
public class Modalidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    private Double creditos;

    public Modalidade() {}

    public Modalidade(Long id, String name, String description) {
        this(id, name, description, null);
    }

    public Modalidade(Long id, String name, String description, Double creditos) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creditos = creditos;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getCreditos() {
        return creditos;
    }

    public void setCreditos(Double creditos) {
        this.creditos = creditos;
    }
}
