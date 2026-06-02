package br.project.portalapo.controller;

import br.project.portalapo.model.Orientador;
import br.project.portalapo.service.OrientadorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orientadores")
@CrossOrigin
public class OrientadorController {

    private final OrientadorService service;

    public OrientadorController(OrientadorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Orientador> getAll() {
        return service.findAll();
    }

    @PostMapping
    public Orientador create(@RequestBody Orientador o) {
        return service.save(o);
    }
}
