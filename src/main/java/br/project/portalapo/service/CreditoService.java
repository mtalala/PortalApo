package br.project.portalapo.service;

import br.project.portalapo.repository.APORepository;
import org.springframework.stereotype.Service;

@Service
public class CreditoService {

    private final APORepository apoRepository;

    public CreditoService(APORepository apoRepository) {
        this.apoRepository = apoRepository;
    }

    public Double getTotalCreditos(Long alunoId) {
        Double total = apoRepository.somarCreditosAprovados(alunoId);
        return total != null ? total : 0.0;
    }

    public boolean atingiuMinimo(Long alunoId) {
        return getTotalCreditos(alunoId) >= 12.0;
    }
}
