package br.project.portalapo.service;

import br.project.portalapo.enums.RoleAprovacao;
import br.project.portalapo.enums.StatusAPO;
import br.project.portalapo.model.APO;
import br.project.portalapo.model.ApprovalItem;
import br.project.portalapo.model.AuditLog;
import br.project.portalapo.model.User;
import br.project.portalapo.repository.APORepository;
import br.project.portalapo.repository.AuditLogRepository;
import br.project.portalapo.repository.UserRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioService {

    private final APORepository apoRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final CreditoService creditoService;

    public RelatorioService(APORepository apoRepository, UserRepository userRepository,
                            AuditLogRepository auditLogRepository, CreditoService creditoService) {
        this.apoRepository = apoRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.creditoService = creditoService;
    }

    public List<Map<String, Object>> creditosAluno(Long alunoUserId) {
        return List.of(Map.of(
                "alunoUserId", alunoUserId,
                "totalCreditos", creditoService.getTotalCreditos(alunoUserId),
                "minimoExigido", 12.0
        ));
    }

    public List<Map<String, Object>> historicoAluno(Long alunoUserId) {
        return apoRepository.findByAlunoUserId(alunoUserId).stream()
                .map(this::apoResumo)
                .toList();
    }

    public List<Map<String, Object>> avaliacoesPorRole(RoleAprovacao role, Long userId) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (APO apo : apoRepository.findAll()) {
            if (apo.getApprovals() == null) {
                continue;
            }
            for (ApprovalItem approval : apo.getApprovals()) {
                if (approval.getRole() == role
                        && (userId == null || String.valueOf(userId).equals(approval.getUserId()))) {
                    rows.add(Map.of(
                            "codigoApo", value(apo.getCodigoApo()),
                            "nomeAluno", value(apo.getNome() != null ? apo.getNome() : apo.getAlunoUsername()),
                            "decisao", approval.isApproved() ? "APROVADA" : "REJEITADA",
                            "justificativa", value(approval.getJustificativa()),
                            "data", value(approval.getAvaliadoEm())
                    ));
                }
            }
        }
        return rows;
    }

    public List<Map<String, Object>> creditosPorAluno() {
        return userRepository.findByRole("ALUNO").stream()
                .map(aluno -> Map.<String, Object>of(
                        "alunoUserId", aluno.getId(),
                        "username", aluno.getUsername(),
                        "totalCreditos", creditoService.getTotalCreditos(aluno.getId())
                ))
                .toList();
    }

    public List<Map<String, Object>> situacaoAlunos() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (User aluno : userRepository.findByRole("ALUNO")) {
            Double total = creditoService.getTotalCreditos(aluno.getId());
            rows.add(Map.of(
                    "alunoUserId", aluno.getId(),
                    "username", aluno.getUsername(),
                    "totalCreditos", total,
                    "situacao", total >= 12.0 ? "MINIMO_ATINGIDO" : "PENDENTE"
            ));
        }
        return rows;
    }

    public List<Map<String, Object>> aposPorStatus(StatusAPO status) {
        return apoRepository.findByStatus(status).stream()
                .map(this::apoResumo)
                .toList();
    }

    public List<Map<String, Object>> historicoCreditosSecretaria() {
        return auditLogRepository.findByAcao("LANCAMENTO_CREDITOS").stream()
                .map(this::auditResumo)
                .toList();
    }

    public byte[] gerarPdf(String titulo, List<Map<String, Object>> dados) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, output);
            document.open();
            document.add(new Paragraph(titulo));
            document.add(new Paragraph("Gerado em " + LocalDate.now()));
            document.add(new Paragraph(" "));
            for (Map<String, Object> row : dados) {
                document.add(new Paragraph(row.toString()));
            }
            document.close();
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar PDF", e);
        }
    }

    private Map<String, Object> apoResumo(APO apo) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", apo.getId());
        row.put("codigoApo", apo.getCodigoApo());
        row.put("alunoUserId", apo.getAlunoUserId());
        row.put("nomeAluno", apo.getNome() != null ? apo.getNome() : apo.getAlunoUsername());
        row.put("semestre", apo.getSemestre());
        row.put("status", apo.getStatus());
        row.put("totalCreditos", apo.getTotalPoints());
        row.put("dataSubmissao", apo.getDataSubmissao());
        row.put("completedAt", apo.getCompletedAt());
        row.put("justificativas", apo.getApprovals() == null ? List.of() : apo.getApprovals().stream()
                .filter(approval -> !approval.isApproved() && approval.getJustificativa() != null)
                .map(ApprovalItem::getJustificativa)
                .toList());
        return row;
    }

    private Map<String, Object> auditResumo(AuditLog log) {
        return Map.of(
                "usuarioId", value(log.getUsuarioId()),
                "usuarioUsername", value(log.getUsuarioUsername()),
                "entidade", value(log.getEntidade()),
                "entidadeId", value(log.getEntidadeId()),
                "detalhe", value(log.getDetalhe()),
                "timestamp", value(log.getTimestamp())
        );
    }

    private Object value(Object value) {
        return value != null ? value : "";
    }
}
