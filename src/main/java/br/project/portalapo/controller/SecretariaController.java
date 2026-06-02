package br.project.portalapo.controller;

import br.project.portalapo.config.JwtUtil;
import br.project.portalapo.model.Secretaria;
import br.project.portalapo.model.User;
import br.project.portalapo.repository.UserRepository;
import br.project.portalapo.service.AuditLogService;
import br.project.portalapo.service.CreditoService;
import br.project.portalapo.service.SecretariaService;
import br.project.portalapo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/secretarias")
@CrossOrigin
public class SecretariaController {

    private final SecretariaService service;
    private final CreditoService creditoService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public SecretariaController(SecretariaService service, CreditoService creditoService,
                                UserRepository userRepository, AuditLogService auditLogService,
                                JwtUtil jwtUtil, UserService userService) {
        this.service = service;
        this.creditoService = creditoService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @GetMapping
    public List<Secretaria> getAll() {
        return service.findAll();
    }

    @PostMapping
    public Secretaria create(@RequestBody Secretaria s) {
        return service.save(s);
    }

    @GetMapping("/alunos-aptos-lancamento")
    public ResponseEntity<List<Map<String, Object>>> alunosAptosParaLancamento() {
        List<Map<String, Object>> alunos = userRepository.findByRole("ALUNO").stream()
                .map(aluno -> {
                    Double total = creditoService.getTotalCreditos(aluno.getId());
                    return Map.<String, Object>of(
                            "alunoId", aluno.getId(),
                            "username", aluno.getUsername(),
                            "totalCreditos", total,
                            "apto", total >= 12.0
                    );
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(alunos);
    }

    @PostMapping("/registrar-lancamento/{alunoUserId}")
    public ResponseEntity<?> registrarLancamento(@PathVariable Long alunoUserId, HttpServletRequest request) {
        Double total = creditoService.getTotalCreditos(alunoUserId);
        if (total < 12.0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Aluno ainda não atingiu 12 créditos para lançamento formal.");
        }

        User currentUser = getCurrentUser(request);
        auditLogService.registrar(currentUser, "LANCAMENTO_CREDITOS", "ALUNO", alunoUserId,
                "Lançamento formal de " + total + " créditos");

        return ResponseEntity.ok(Map.of(
                "alunoUserId", alunoUserId,
                "totalCreditos", total,
                "dataLancamento", LocalDateTime.now()
        ));
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
