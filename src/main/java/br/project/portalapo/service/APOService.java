package br.project.portalapo.service;

import br.project.portalapo.enums.RoleAprovacao;
import br.project.portalapo.enums.StatusAPO;
import br.project.portalapo.model.APO;
import br.project.portalapo.model.ApprovalItem;
import br.project.portalapo.model.FileItem;
import br.project.portalapo.model.User;
import br.project.portalapo.repository.APORepository;
import br.project.portalapo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class APOService {

    private final APORepository apoRepository;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    public APOService(APORepository apoRepository) {
        this(apoRepository, null, null, null);
    }

    @Autowired
    public APOService(APORepository apoRepository, AuditLogService auditLogService,
                      UserRepository userRepository, JavaMailSender mailSender) {
        this.apoRepository = apoRepository;
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    @Value("${app.frontend.url:http://localhost:8081}")
    private String frontendUrl;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "APO não encontrada: " + id));
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
        validateDuplicateHashes(apo);

        APO saved = apoRepository.save(apo);
        registrar(currentUser, "SUBMISSAO", saved.getId(), "Submissão da APO " + saved.getCodigoApo());
        return saved;
    }

    public APO create(APO apo) {
        return create(apo, null);
    }

    public APO update(Long id, APO updated, User currentUser) {
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
        apo.setDataAtividade(updated.getDataAtividade());

        apo.setActivities(updated.getActivities());
        apo.setFiles(updated.getFiles());
        apo.setApprovals(updated.getApprovals());

        apo.calcularTotalPoints();

        APO saved = apoRepository.save(apo);
        registrar(currentUser, "ALTERACAO", saved.getId(), "Alteração da APO " + saved.getCodigoApo());
        return saved;
    }

    public APO update(Long id, APO updated) {
        return update(id, updated, null);
    }

    public void delete(Long id) {
        apoRepository.delete(findById(id));
    }

    public APO addFile(Long id, FileItem fileItem, User currentUser) {
        APO apo = findById(id, currentUser);
        if (apo.getFiles() == null) {
            apo.setFiles(new ArrayList<>());
        }
        apo.getFiles().add(fileItem);
        APO saved = apoRepository.save(apo);
        registrar(currentUser, "ALTERACAO", saved.getId(), "Upload de evidência " + fileItem.getName());
        return saved;
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

        APO saved = apoRepository.save(apo);
        registrar(currentUser, "APROVACAO", saved.getId(), "Avaliação do orientador");
        notificarAluno(saved, approved ? "Aprovado pelo orientador" : "Reprovado pelo orientador", null);
        return saved;
    }

    public APO aprovarCoordenador(Long id, User currentUser) {
        requireRole(currentUser, "COORDENADOR");
        APO apo = findById(id);
        requireStatus(apo, StatusAPO.PENDENTE_COORDENACAO);
        addApproval(apo, currentUser, RoleAprovacao.COORDENADOR, true);
        apo.aprovarCoordenador();
        APO saved = apoRepository.save(apo);
        registrar(currentUser, "APROVACAO", saved.getId(), "Aprovação do coordenador");
        notificarAluno(saved, "Aprovado", null);
        return saved;
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
        APO saved = apoRepository.save(apo);
        registrar(currentUser, "APROVACAO", saved.getId(), "Aprovação da comissão APO");
        notificarAluno(saved, "Aprovado pela comissão", null);
        return saved;
    }

    public APO aprovarComissao(Long id) {
        APO apo = findById(id);
        apo.aprovarComissao();
        return apoRepository.save(apo);
    }

    public APO rejeitar(Long id, User currentUser, String justificativa) {
        if (isAluno(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Aluno não pode rejeitar APO");
        }
        if (justificativa == null || justificativa.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Justificativa é obrigatória para reprovação.");
        }

        APO apo = findById(id);
        RoleAprovacao role = roleAprovacaoFromUser(currentUser);
        if (role != null) {
            addApproval(apo, currentUser, role, false, justificativa);
        }
        apo.rejeitar();
        APO saved = apoRepository.save(apo);
        registrar(currentUser, "REPROVACAO", saved.getId(), justificativa);
        notificarAluno(saved, "Reprovado", justificativa);
        return saved;
    }

    public APO rejeitar(Long id, User currentUser) {
        return rejeitar(id, currentUser, "Reprovação sem justificativa registrada.");
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

    private boolean isSecretaria(User user) {
        return user != null && "SECRETARIA".equalsIgnoreCase(user.getRole());
    }

    private boolean isAdmin(User user) {
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    public boolean canAccessFile(APO apo, User user) {
        if (user == null) {
            return false;
        }

        return isAdmin(user)
                || isSecretaria(user)
                || isCoordenador(user)
                || isComissao(user)
                || (isAluno(user) && user.getId().equals(apo.getAlunoUserId()))
                || (isOrientador(user) && apo.getOrientadorUserIds() != null
                    && apo.getOrientadorUserIds().contains(user.getId()));
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

        addApproval(apo, user, role, approved, null);
    }

    private void addApproval(APO apo, User user, RoleAprovacao role, boolean approved, String justificativa) {
        if (apo.getApprovals() == null) {
            apo.setApprovals(new ArrayList<>());
        }

        boolean alreadyReviewed = user != null && apo.getApprovals().stream()
                .anyMatch(approval -> String.valueOf(user.getId()).equals(approval.getUserId())
                        && approval.getRole() == role);

        if (alreadyReviewed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já avaliou esta APO");
        }

        apo.getApprovals().add(new ApprovalItem(String.valueOf(user.getId()), role, approved, justificativa));
    }

    private void validateDuplicateHashes(APO apo) {
        if (apo.getFiles() == null || apo.getAlunoUserId() == null || apo.getSemestre() == null) {
            return;
        }

        for (FileItem file : apo.getFiles()) {
            if (file != null && file.getHash() != null && !file.getHash().isBlank()
                    && apoRepository.existsDuplicateByHash(apo.getAlunoUserId(), apo.getSemestre(), file.getHash())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Submissão duplicada: já existe uma APO com o mesmo documento neste semestre para esta modalidade.");
            }
        }
    }

    private RoleAprovacao roleAprovacaoFromUser(User user) {
        if (isOrientador(user)) {
            return RoleAprovacao.ORIENTADOR;
        }
        if (isComissao(user)) {
            return RoleAprovacao.COMISSAO;
        }
        if (isCoordenador(user)) {
            return RoleAprovacao.COORDENADOR;
        }
        return null;
    }

    private void registrar(User user, String acao, Long entidadeId, String detalhe) {
        if (auditLogService != null) {
            auditLogService.registrar(user, acao, "APO", entidadeId, detalhe);
        }
    }

    @Async
    public void notificarAluno(APO apo, String resultado, String justificativa) {
        if (mailSender == null || userRepository == null || apo == null || apo.getAlunoUserId() == null) {
            return;
        }

        userRepository.findById(apo.getAlunoUserId()).ifPresent(aluno -> {
            if (aluno.getEmail() == null || aluno.getEmail().isBlank()) {
                return;
            }

            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(aluno.getEmail());
                message.setSubject("Portal APO — Atualização da sua solicitação " + apo.getCodigoApo());
                String body = "APO: " + apo.getCodigoApo() + "\n"
                        + "Resultado: " + resultado + "\n"
                        + (justificativa != null && !justificativa.isBlank()
                            ? "Justificativa: " + justificativa + "\n"
                            : "")
                        + "Acesse o portal: " + frontendUrl;
                message.setText(body);
                mailSender.send(message);
            } catch (Exception ignored) {
            }
        });
    }
}
