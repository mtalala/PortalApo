package br.project.portalapo.model;

import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class Aluno extends Pessoa {

    private String ra;
    private String curso;
    private Integer periodo;
    private LocalDate dataIngresso;
    private LocalDate dataProvavelSaida;

    public Aluno() {
        super();
    }

    public Aluno(Long id, String nome, String email, boolean ativo, String ra, String curso, Integer periodo) {
        this(id, nome, email, ativo, ra, curso, periodo, null, null);
    }

    public Aluno(Long id, String nome, String email, boolean ativo, String ra, String curso, Integer periodo,
                 LocalDate dataIngresso, LocalDate dataProvavelSaida) {
        super(id, nome, email, ativo);
        this.ra = ra;
        this.curso = curso;
        this.periodo = periodo;
        this.dataIngresso = dataIngresso;
        this.dataProvavelSaida = dataProvavelSaida;
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

    public LocalDate getDataIngresso() {
        return dataIngresso;
    }

    public void setDataIngresso(LocalDate dataIngresso) {
        this.dataIngresso = dataIngresso;
    }

    public LocalDate getDataProvavelSaida() {
        return dataProvavelSaida;
    }

    public void setDataProvavelSaida(LocalDate dataProvavelSaida) {
        this.dataProvavelSaida = dataProvavelSaida;
    }
}
