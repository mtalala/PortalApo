package br.project.portalapo.service;

import br.project.portalapo.model.Orientador;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrientadorService {

    private final List<Orientador> orientadores = new ArrayList<>();

    public List<Orientador> findAll() {
        return orientadores;
    }

    public Orientador save(Orientador o) {
        orientadores.add(o);
        return o;
    }
}
