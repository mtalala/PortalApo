package br.project.portalapo.model;

import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class Coordenador extends Pessoa {

    private LocalDate mandatoInicio;
    private LocalDate mandatoFim;

    public Coordenador() {
        super();
    }

    public Coordenador(Long id, String nome, String email, boolean ativo, LocalDate mandatoInicio, LocalDate mandatoFim) {
        super(id, nome, email, ativo);
        this.mandatoInicio = mandatoInicio;
        this.mandatoFim = mandatoFim;
    }

    public LocalDate getMandatoInicio() {
        return mandatoInicio;
    }

    public void setMandatoInicio(LocalDate mandatoInicio) {
        this.mandatoInicio = mandatoInicio;
    }

    public LocalDate getMandatoFim() {
        return mandatoFim;
    }

    public void setMandatoFim(LocalDate mandatoFim) {
        this.mandatoFim = mandatoFim;
    }
}