package br.project.portalapo.controller;

import br.project.portalapo.model.MembroComite;
import br.project.portalapo.service.MembroComiteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comissao")
@CrossOrigin
public class MembroComiteController {

    private final MembroComiteService service;

    public MembroComiteController(MembroComiteService service) {
        this.service = service;
    }

    @GetMapping
    public List<MembroComite> getAll() {
        return service.findAll();
    }

    @PostMapping
    public MembroComite create(@RequestBody MembroComite m) {
        return service.save(m);
    }
}
