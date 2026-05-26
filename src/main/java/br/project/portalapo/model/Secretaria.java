package br.project.portalapo.model;

import jakarta.persistence.Entity;

@Entity
public class Secretaria extends Pessoa {

    private String setor;

    public Secretaria() {
        super();
    }

    public Secretaria(Long id, String nome, String email, boolean ativo, String setor) {
        super(id, nome, email, ativo);
        this.setor = setor;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }
}