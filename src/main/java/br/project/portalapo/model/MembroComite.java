package br.project.portalapo.model;

import jakarta.persistence.Entity;

@Entity
public class MembroComite extends Pessoa {

    private String funcao;

    public MembroComite() {
        super();
    }

    public MembroComite(Long id, String nome, String email, boolean ativo, String funcao) {
        super(id, nome, email, ativo);
        this.funcao = funcao;
    }

    public String getFuncao() {
        return funcao;
    }

    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }
}