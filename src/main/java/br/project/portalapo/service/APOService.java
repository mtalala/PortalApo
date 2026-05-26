package br.project.portalapo.service;

import br.project.portalapo.enums.RoleAprovacao;
import br.project.portalapo.enums.StatusAPO;
import br.project.portalapo.model.APO;
import br.project.portalapo.model.ApprovalItem;
import br.project.portalapo.model.User;
import br.project.portalapo.repository.APORepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
public class APOService {

    private final APORepository apoRepository;

    public APOService(APORepository apoRepository) {
        this.apoRepository = apoRepository;
    }

    // =========================
    // CRUD
    // =========================

    public List<APO> findAll(User currentUser) {
        if (currentUser == null) {
            return Collections.emptyList();
        }

        if (isAluno(currentUser)) {
            return apoRepository.findByAlunoUserId(currentUser.getId());
        }

        return apoRepository.findAll();
    }

    public List<APO> findAll() {
        return findAll(null);
    }

    public APO findById(Long id, User currentUser) {
        APO apo = findById(id);

        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não autenticado");
        }

        if (isAluno(currentUser) && !currentUser.getId().equals(apo.getAlunoUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado à APO: " + id);
        }

        return apo;
    }

    public APO findById(Long id) {
        return apoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("APO não encontrada: " + id));
    }

    public APO create(APO apo, User currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não autenticado");
        }

        if (isAluno(currentUser)) {
            apo.setAlunoUserId(currentUser.getId());
            apo.setAlunoUsername(currentUser.getUsername());
        }

        if (apo.getStatus() == null) {
            apo.setStatus(StatusAPO.PENDENTE_ORIENTADOR);
        }

        apo.calcularTotalPoints();
        return apoRepository.save(apo);
    }

    public APO create(APO apo) {
        return create(apo, null);
    }

    public APO update(Long id, APO updated) {
        APO apo = findById(id);

        apo.setCodigoApo(updated.getCodigoApo());
        apo.setNome(updated.getNome());
        apo.setMatricula(updated.getMatricula());
        apo.setProgram(updated.getProgram());
        apo.setSemestre(updated.getSemestre());
        apo.setOrientador(updated.getOrientador());
        apo.setCoordenador(updated.getCoordenador());

        apo.setActivities(updated.getActivities());
        apo.setFiles(updated.getFiles());
        apo.setApprovals(updated.getApprovals());

        apo.calcularTotalPoints();

        return apoRepository.save(apo);
    }

    public void delete(Long id) {
        apoRepository.delete(findById(id));
    }

    // =========================
    // FLUXO
    // =========================

    public APO aprovarOrientador(Long id, User currentUser) {
        requireRole(currentUser, "ORIENTADOR");
        APO apo = findById(id);
        requireStatus(apo, StatusAPO.PENDENTE_ORIENTADOR);
        addApproval(apo, currentUser, RoleAprovacao.ORIENTADOR);
        apo.aprovarOrientador();
        return apoRepository.save(apo);
    }

    public APO aprovarOrientador(Long id) {
        APO apo = findById(id);
        apo.aprovarOrientador();
        return apoRepository.save(apo);
    }

    public APO aprovarCoordenador(Long id, User currentUser) {
        requireRole(currentUser, "COORDENADOR");
        APO apo = findById(id);
        requireStatus(apo, StatusAPO.PENDENTE_COORDENACAO);
        addApproval(apo, currentUser, RoleAprovacao.COORDENADOR);
        apo.aprovarCoordenador();
        return apoRepository.save(apo);
    }

    public APO aprovarCoordenador(Long id) {
        APO apo = findById(id);
        apo.aprovarCoordenador();
        return apoRepository.save(apo);
    }

    public APO aprovarComissao(Long id, User currentUser) {
        requireRole(currentUser, "COMISSAO");
        APO apo = findById(id);
        requireStatus(apo, StatusAPO.PENDENTE_COMISSAO);
        addApproval(apo, currentUser, RoleAprovacao.COMISSAO);
        apo.aprovarComissao();
        return apoRepository.save(apo);
    }

    public APO aprovarComissao(Long id) {
        APO apo = findById(id);
        apo.aprovarComissao();
        return apoRepository.save(apo);
    }

    public APO rejeitar(Long id, User currentUser) {
        if (isAluno(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Aluno não pode rejeitar APO");
        }

        APO apo = findById(id);
        apo.rejeitar();
        return apoRepository.save(apo);
    }

    public APO rejeitar(Long id) {
        APO apo = findById(id);
        apo.rejeitar();
        return apoRepository.save(apo);
    }

    private boolean isAluno(User user) {
        return user != null && "ALUNO".equalsIgnoreCase(user.getRole());
    }

    private void requireRole(User user, String role) {
        if (user == null || !role.equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado para a etapa: " + role);
        }
    }

    private void requireStatus(APO apo, StatusAPO status) {
        if (apo.getStatus() != status) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "APO não está na etapa esperada: " + status);
        }
    }

    private void addApproval(APO apo, User user, RoleAprovacao role) {
        boolean alreadyApproved = apo.getApprovals() != null && apo.getApprovals().stream()
                .anyMatch(approval -> String.valueOf(user.getId()).equals(approval.getUserId())
                        && approval.getRole() == role);

        if (alreadyApproved) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já aprovou esta APO");
        }

        apo.getApprovals().add(new ApprovalItem(String.valueOf(user.getId()), role, true));
    }
}
