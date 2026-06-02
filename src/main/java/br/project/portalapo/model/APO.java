package br.project.portalapo.model;

import br.project.portalapo.enums.RoleAprovacao;
import br.project.portalapo.enums.StatusAPO;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class APO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigoApo;

    private String nome;
    private String matricula;
    private Long alunoUserId;
    private String alunoUsername;

    private String program;
    private String semestre;

    private String orientador;
    private String coordenador;

    @ElementCollection
    @CollectionTable(name = "apo_orientadores", joinColumns = @JoinColumn(name = "apo_id"))
    @Column(name = "orientador_user_id")
    private List<Long> orientadorUserIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "apo_orientador_usernames", joinColumns = @JoinColumn(name = "apo_id"))
    @Column(name = "orientador_username")
    private List<String> orientadorUsernames = new ArrayList<>();

    private Long coordenadorUserId;
    private String coordenadorUsername;

    @Enumerated(EnumType.STRING)
    private StatusAPO status;

    private LocalDate dataSubmissao;
    private LocalDate dataAtividade;
    private LocalDate completedAt;
    private Double totalPoints = 0.0;
    private Integer requiredCommissionApprovals = 3;

    // =========================
    // LISTAS DO JSON
    // =========================

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            name = "apo_activities",
            joinColumns = @JoinColumn(name = "apo_id"),
            inverseJoinColumns = @JoinColumn(name = "activities_id")
    )
    private List<ActivityItem> activities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "apo_files", joinColumns = @JoinColumn(name = "apo_id"))
    private List<FileItem> files = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "apo_approvals", joinColumns = @JoinColumn(name = "apo_id"))
    private List<ApprovalItem> approvals = new ArrayList<>();

    // Constructors
    public APO() {
        this.activities = new ArrayList<>();
        this.files = new ArrayList<>();
        this.approvals = new ArrayList<>();
        this.totalPoints = 0.0;
        this.requiredCommissionApprovals = 3;
    }

    public APO(Long id, String codigoApo, String nome, String matricula, Long alunoUserId,
               String alunoUsername, String program,
               String semestre, String orientador, String coordenador, StatusAPO status,
               LocalDate dataSubmissao, LocalDate dataAtividade, LocalDate completedAt, Number totalPoints,
               Integer requiredCommissionApprovals, List<ActivityItem> activities,
               List<FileItem> files, List<ApprovalItem> approvals) {
        this.id = id;
        this.codigoApo = codigoApo;
        this.nome = nome;
        this.matricula = matricula;
        this.alunoUserId = alunoUserId;
        this.alunoUsername = alunoUsername;
        this.program = program;
        this.semestre = semestre;
        this.orientador = orientador;
        this.coordenador = coordenador;
        this.status = status;
        this.dataSubmissao = dataSubmissao;
        this.dataAtividade = dataAtividade;
        this.completedAt = completedAt;
        this.totalPoints = totalPoints != null ? totalPoints.doubleValue() : 0.0;
        this.requiredCommissionApprovals = requiredCommissionApprovals;
        this.activities = activities != null ? activities : new ArrayList<>();
        this.files = files != null ? files : new ArrayList<>();
        this.approvals = approvals != null ? approvals : new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoApo() {
        return codigoApo;
    }

    public void setCodigoApo(String codigoApo) {
        this.codigoApo = codigoApo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public Long getAlunoUserId() {
        return alunoUserId;
    }

    public void setAlunoUserId(Long alunoUserId) {
        this.alunoUserId = alunoUserId;
    }

    public String getAlunoUsername() {
        return alunoUsername;
    }

    public void setAlunoUsername(String alunoUsername) {
        this.alunoUsername = alunoUsername;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getSemestre() {
        return semestre;
    }

    public void setSemestre(String semestre) {
        this.semestre = semestre;
    }

    public String getOrientador() {
        return orientador;
    }

    public void setOrientador(String orientador) {
        this.orientador = orientador;
    }

    public String getCoordenador() {
        return coordenador;
    }

    public void setCoordenador(String coordenador) {
        this.coordenador = coordenador;
    }

    public List<Long> getOrientadorUserIds() {
        return orientadorUserIds;
    }

    public void setOrientadorUserIds(List<Long> orientadorUserIds) {
        this.orientadorUserIds = orientadorUserIds;
    }

    public List<String> getOrientadorUsernames() {
        return orientadorUsernames;
    }

    public void setOrientadorUsernames(List<String> orientadorUsernames) {
        this.orientadorUsernames = orientadorUsernames;
    }

    public Long getCoordenadorUserId() {
        return coordenadorUserId;
    }

    public void setCoordenadorUserId(Long coordenadorUserId) {
        this.coordenadorUserId = coordenadorUserId;
    }

    public String getCoordenadorUsername() {
        return coordenadorUsername;
    }

    public void setCoordenadorUsername(String coordenadorUsername) {
        this.coordenadorUsername = coordenadorUsername;
    }

    public StatusAPO getStatus() {
        return status;
    }

    public void setStatus(StatusAPO status) {
        this.status = status;
    }

    public LocalDate getDataSubmissao() {
        return dataSubmissao;
    }

    public void setDataSubmissao(LocalDate dataSubmissao) {
        this.dataSubmissao = dataSubmissao;
    }

    public LocalDate getDataAtividade() {
        return dataAtividade;
    }

    public void setDataAtividade(LocalDate dataAtividade) {
        this.dataAtividade = dataAtividade;
    }

    public LocalDate getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDate completedAt) {
        this.completedAt = completedAt;
    }

    public Double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Number totalPoints) {
        this.totalPoints = totalPoints != null ? totalPoints.doubleValue() : null;
    }

    public Integer getRequiredCommissionApprovals() {
        return requiredCommissionApprovals;
    }

    public void setRequiredCommissionApprovals(Integer requiredCommissionApprovals) {
        this.requiredCommissionApprovals = requiredCommissionApprovals;
    }

    public List<ActivityItem> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityItem> activities) {
        this.activities = activities;
    }

    public List<FileItem> getFiles() {
        return files;
    }

    public void setFiles(List<FileItem> files) {
        this.files = files;
    }

    public List<ApprovalItem> getApprovals() {
        return approvals;
    }

    public void setApprovals(List<ApprovalItem> approvals) {
        this.approvals = approvals;
    }

    public long getOrientadorEvaluationsCount() {
        if (approvals == null) {
            return 0;
        }

        return approvals.stream()
                .filter(approval -> approval.getRole() == RoleAprovacao.ORIENTADOR)
                .map(ApprovalItem::getUserId)
                .distinct()
                .count();
    }

    public long getComissaoEvaluationsCount() {
        if (approvals == null) {
            return 0;
        }

        return approvals.stream()
                .filter(approval -> approval.getRole() == RoleAprovacao.COMISSAO)
                .map(ApprovalItem::getUserId)
                .distinct()
                .count();
    }

    public boolean hasOrientadorReview(String userId) {
        if (approvals == null || userId == null) {
            return false;
        }

        return approvals.stream()
                .filter(approval -> approval.getRole() == RoleAprovacao.ORIENTADOR)
                .anyMatch(approval -> userId.equals(approval.getUserId()));
    }









    // =========================
    // REGRAS
    // =========================

    public void calcularTotalPoints() {
        double total = 0;

        if (activities != null) {
            for (ActivityItem a : activities) {
                if (a != null && a.getPoints() != null) {
                    total += a.getPoints();
                }
            }
        }

        this.totalPoints = total;
    }

    public void aprovarOrientador() {
        this.status = StatusAPO.PENDENTE_COMISSAO;
    }

    public void aprovarCoordenador() {
        this.status = StatusAPO.APROVADA;
        this.completedAt = LocalDate.now();
    }

    public void aprovarComissao() {
        this.status = StatusAPO.PENDENTE_COORDENACAO;
    }

    public void rejeitar() {
        this.status = StatusAPO.REJEITADA;
        this.completedAt = LocalDate.now();
    }
}
