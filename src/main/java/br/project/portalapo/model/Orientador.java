package br.project.portalapo.model;

import jakarta.persistence.Entity;

@Entity
public class Orientador extends Pessoa {

    private String areaAtuacao;
    private String titulacao;

    public Orientador() {
        super();
    }

    public Orientador(Long id, String nome, String email, boolean ativo, String areaAtuacao, String titulacao) {
        super(id, nome, email, ativo);
        this.areaAtuacao = areaAtuacao;
        this.titulacao = titulacao;
    }

    public String getAreaAtuacao() {
        return areaAtuacao;
    }

    public void setAreaAtuacao(String areaAtuacao) {
        this.areaAtuacao = areaAtuacao;
    }

    public String getTitulacao() {
        return titulacao;
    }

    public void setTitulacao(String titulacao) {
        this.titulacao = titulacao;
    }
}
