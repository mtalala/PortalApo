package br.project.portalapo.model;

import jakarta.persistence.Entity;

@Entity
public class Administrador extends Pessoa {

    private String nivelAcesso;

    public Administrador() {
        super();
    }

    public Administrador(Long id, String nome, String email, boolean ativo, String nivelAcesso) {
        super(id, nome, email, ativo);
        this.nivelAcesso = nivelAcesso;
    }

    public String getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(String nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }
}