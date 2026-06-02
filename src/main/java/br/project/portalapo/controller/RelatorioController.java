package br.project.portalapo.controller;

import br.project.portalapo.config.JwtUtil;
import br.project.portalapo.enums.RoleAprovacao;
import br.project.portalapo.enums.StatusAPO;
import br.project.portalapo.model.User;
import br.project.portalapo.service.RelatorioService;
import br.project.portalapo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public RelatorioController(RelatorioService relatorioService, JwtUtil jwtUtil, UserService userService) {
        this.relatorioService = relatorioService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @GetMapping("/aluno/{alunoUserId}/creditos")
    public ResponseEntity<?> creditosAluno(@PathVariable Long alunoUserId,
                                           @RequestParam(required = false) String formato,
                                           HttpServletRequest request) {
        User user = requireCurrentUser(request);
        requireOwnAlunoOr(user, alunoUserId, "COORDENADOR", "SECRETARIA", "ADMIN");
        return respond("creditos_aluno", relatorioService.creditosAluno(alunoUserId), formato);
    }

    @GetMapping("/aluno/{alunoUserId}/historico")
    public ResponseEntity<?> historicoAluno(@PathVariable Long alunoUserId,
                                            @RequestParam(required = false) String formato,
                                            HttpServletRequest request) {
        User user = requireCurrentUser(request);
        requireOwnAlunoOr(user, alunoUserId, "ORIENTADOR", "COORDENADOR", "SECRETARIA", "ADMIN");
        return respond("historico_aluno", relatorioService.historicoAluno(alunoUserId), formato);
    }

    @GetMapping("/orientador/{orientadorUserId}/avaliacoes")
    public ResponseEntity<?> avaliacoesOrientador(@PathVariable Long orientadorUserId,
                                                  @RequestParam(required = false) String formato,
                                                  HttpServletRequest request) {
        User user = requireCurrentUser(request);
        requireOwnRoleOr(user, orientadorUserId, "ORIENTADOR", "COORDENADOR", "ADMIN");
        return respond("avaliacoes_orientador",
                relatorioService.avaliacoesPorRole(RoleAprovacao.ORIENTADOR, orientadorUserId), formato);
    }

    @GetMapping("/comissao/avaliacoes")
    public ResponseEntity<?> avaliacoesComissao(@RequestParam(required = false) String formato,
                                                HttpServletRequest request) {
        User user = requireCurrentUser(request);
        requireRole(user, "COMISSAO", "COORDENADOR", "ADMIN");
        return respond("avaliacoes_comissao",
                relatorioService.avaliacoesPorRole(RoleAprovacao.COMISSAO, null), formato);
    }

    @GetMapping("/coordenador/creditos-por-aluno")
    public ResponseEntity<?> creditosPorAluno(@RequestParam(required = false) String formato,
                                              HttpServletRequest request) {
        requireRole(requireCurrentUser(request), "COORDENADOR", "ADMIN");
        return respond("creditos_por_aluno", relatorioService.creditosPorAluno(), formato);
    }

    @GetMapping("/coordenador/situacao-alunos")
    public ResponseEntity<?> situacaoAlunos(@RequestParam(required = false) String formato,
                                            HttpServletRequest request) {
        requireRole(requireCurrentUser(request), "COORDENADOR", "ADMIN");
        return respond("situacao_alunos", relatorioService.situacaoAlunos(), formato);
    }

    @GetMapping("/coordenador/apos-aprovadas")
    public ResponseEntity<?> aposAprovadas(@RequestParam(required = false) String formato,
                                           HttpServletRequest request) {
        requireRole(requireCurrentUser(request), "COORDENADOR", "ADMIN");
        return respond("apos_aprovadas", relatorioService.aposPorStatus(StatusAPO.APROVADA), formato);
    }

    @GetMapping("/coordenador/apos-rejeitadas")
    public ResponseEntity<?> aposRejeitadas(@RequestParam(required = false) String formato,
                                            HttpServletRequest request) {
        requireRole(requireCurrentUser(request), "COORDENADOR", "ADMIN");
        return respond("apos_rejeitadas", relatorioService.aposPorStatus(StatusAPO.REJEITADA), formato);
    }

    @GetMapping("/secretaria/historico-creditos")
    public ResponseEntity<?> historicoCreditos(@RequestParam(required = false) String formato,
                                               HttpServletRequest request) {
        requireRole(requireCurrentUser(request), "SECRETARIA", "ADMIN");
        return respond("historico_creditos", relatorioService.historicoCreditosSecretaria(), formato);
    }

    private ResponseEntity<?> respond(String tipo, List<Map<String, Object>> dados, String formato) {
        if ("pdf".equalsIgnoreCase(formato)) {
            byte[] pdf = relatorioService.gerarPdf("Relatório " + tipo, dados);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"relatorio_" + tipo + "_" + LocalDate.now() + ".pdf\"")
                    .body(pdf);
        }
        return ResponseEntity.ok(dados);
    }

    private void requireOwnAlunoOr(User user, Long alunoUserId, String... roles) {
        if ("ALUNO".equalsIgnoreCase(user.getRole()) && user.getId().equals(alunoUserId)) {
            return;
        }
        requireRole(user, roles);
    }

    private void requireOwnRoleOr(User user, Long ownerId, String ownRole, String... elevatedRoles) {
        if (ownRole.equalsIgnoreCase(user.getRole()) && user.getId().equals(ownerId)) {
            return;
        }
        requireRole(user, elevatedRoles);
    }

    private void requireRole(User user, String... roles) {
        for (String role : roles) {
            if (role.equalsIgnoreCase(user.getRole())) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado ao relatório.");
    }

    private User requireCurrentUser(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não autenticado.");
        }
        return user;
    }

    private User getCurrentUser(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            String username = null;
            if (principal instanceof UserDetails userDetails) {
                username = userDetails.getUsername();
            } else if (principal instanceof String name) {
                username = name;
            }
            if (username != null) {
                return userService.findByUsername(username).orElse(null);
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String username = jwtUtil.extractUsername(authHeader.substring(7));
                return userService.findByUsername(username).orElse(null);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
