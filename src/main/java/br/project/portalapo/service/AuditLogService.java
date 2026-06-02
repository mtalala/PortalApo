package br.project.portalapo.service;

import br.project.portalapo.model.AuditLog;
import br.project.portalapo.model.User;
import br.project.portalapo.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void registrar(User user, String acao, String entidade, Long entidadeId, String detalhe) {
        auditLogRepository.save(new AuditLog(user, acao, entidade, entidadeId, detalhe));
    }
}
