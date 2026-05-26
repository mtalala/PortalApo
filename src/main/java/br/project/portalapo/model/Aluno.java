package br.project.portalapo.model;

import jakarta.persistence.Entity;

@Entity
public class Aluno extends Pessoa {

    private String ra;
    private String curso;
    private Integer periodo;

    public Aluno() {
        super();
    }

    public Aluno(Long id, String nome, String email, boolean ativo, String ra, String curso, Integer periodo) {
        super(id, nome, email, ativo);
        this.ra = ra;
        this.curso = curso;
        this.periodo = periodo;
    }

    public String getRa() {
        return ra;
    }

    public void setRa(String ra) {
        this.ra = ra;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public Integer getPeriodo() {
        return periodo;
    }

    public void setPeriodo(Integer periodo) {
        this.periodo = periodo;
    }
}