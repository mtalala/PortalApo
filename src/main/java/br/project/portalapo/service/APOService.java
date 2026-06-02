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

import java.time.LocalDate;
import java.util.ArrayList;
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

        if (isOrientador(currentUser)) {
            return apoRepository.findByOrientadorUserIdsContaining(currentUser.getId());
        }

        if (isComissao(currentUser)) {
            // Committee members should only see APOs that are awaiting committee review
            return apoRepository.findByStatus(StatusAPO.PENDENTE_COMISSAO);
        }

        if (isCoordenador(currentUser)) {
            if (currentUser.getId() != null) {
                return apoRepository.findByCoordenadorUserIdAndStatusNot(currentUser.getId(), StatusAPO.PENDENTE_ORIENTADOR);
            }
            return apoRepository.findByCoordenadorUsernameAndStatusNot(currentUser.getUsername(), StatusAPO.PENDENTE_ORIENTADOR);
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

        if (apo.getRequiredCommissionApprovals() == null) {
            apo.setRequiredCommissionApprovals(3);
        }

        if (apo.getDataSubmissao() == null) {
            apo.setDataSubmissao(LocalDate.now());
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
        apo.setOrientadorUserIds(updated.getOrientadorUserIds());
        apo.setOrientadorUsernames(updated.getOrientadorUsernames());
        apo.setCoordenadorUserId(updated.getCoordenadorUserId());
        apo.setCoordenadorUsername(updated.getCoordenadorUsername());

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
        return avaliarOrientador(id, currentUser, true);
    }

    public APO aprovarOrientador(Long id) {
        APO apo = findById(id);
        apo.aprovarOrientador();
        return apoRepository.save(apo);
    }

    public APO avaliarOrientador(Long id, User currentUser, boolean approved) {
        requireRole(currentUser, "ORIENTADOR");
        APO apo = findById(id);
        requireStatus(apo, StatusAPO.PENDENTE_ORIENTADOR);
        
        // Ensure approvals list is initialized
        if (apo.getApprovals() == null) {
            apo.setApprovals(new ArrayList<>());
        }
        
        addApproval(apo, currentUser, RoleAprovacao.ORIENTADOR, approved);
        // If all assigned orientadores have evaluated, move to committee review
        long requiredOrientadorApprovals = apo.getOrientadorUserIds() == null
                ? 0
                : apo.getOrientadorUserIds().stream().distinct().count();

        if (requiredOrientadorApprovals > 0 && apo.getOrientadorEvaluationsCount() >= requiredOrientadorApprovals) {
            apo.aprovarOrientador();
        }

        return apoRepository.save(apo);
    }

    public APO aprovarCoordenador(Long id, User currentUser) {
        requireRole(currentUser, "COORDENADOR");
        APO apo = findById(id);
        requireStatus(apo, StatusAPO.PENDENTE_COORDENACAO);
        addApproval(apo, currentUser, RoleAprovacao.COORDENADOR, true);
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
        addApproval(apo, currentUser, RoleAprovacao.COMISSAO, true);
        // Only move to coordinator review when required number of committee approvals reached
        Integer required = apo.getRequiredCommissionApprovals() == null ? 3 : apo.getRequiredCommissionApprovals();
        if (apo.getComissaoEvaluationsCount() >= required) {
            apo.aprovarComissao();
        }
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

    private boolean isOrientador(User user) {
        return user != null && "ORIENTADOR".equalsIgnoreCase(user.getRole());
    }

    private boolean isCoordenador(User user) {
        return user != null && "COORDENADOR".equalsIgnoreCase(user.getRole());
    }

    private boolean isComissao(User user) {
        return user != null && "COMISSAO".equalsIgnoreCase(user.getRole());
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

    private void addApproval(APO apo, User user, RoleAprovacao role, boolean approved) {
        boolean alreadyReviewed = apo.getApprovals() != null && apo.getApprovals().stream()
                .anyMatch(approval -> String.valueOf(user.getId()).equals(approval.getUserId())
                        && approval.getRole() == role);

        if (alreadyReviewed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já avaliou esta APO");
        }

        apo.getApprovals().add(new ApprovalItem(String.valueOf(user.getId()), role, approved));
    }
}
