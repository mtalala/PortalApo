package br.project.portalapo.controller;

import br.project.portalapo.config.JwtUtil;
import br.project.portalapo.model.APO;
import br.project.portalapo.model.FileItem;
import br.project.portalapo.model.User;
import br.project.portalapo.service.AuditLogService;
import br.project.portalapo.service.APOService;
import br.project.portalapo.service.FileStorageService;
import br.project.portalapo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/apos")
public class APOController {

    private final APOService apoService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

    public APOController(APOService apoService, JwtUtil jwtUtil, UserService userService) {
        this(apoService, jwtUtil, userService, null, null);
    }

    @Autowired
    public APOController(APOService apoService, JwtUtil jwtUtil, UserService userService,
                         FileStorageService fileStorageService, AuditLogService auditLogService) {
        this.apoService = apoService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.auditLogService = auditLogService;
    }

    // =========================
    // CRUD
    // =========================

    @GetMapping
    public ResponseEntity<List<APO>> getAll(HttpServletRequest request) {
        return ResponseEntity.ok(apoService.findAll(getCurrentUser(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APO> getById(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(apoService.findById(id, getCurrentUser(request)));
    }

    @PostMapping
    public ResponseEntity<APO> create(@RequestBody APO apo, HttpServletRequest request) {
        return ResponseEntity.ok(apoService.create(apo, getCurrentUser(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<APO> update(@PathVariable Long id,
                                       @RequestBody APO apo,
                                       HttpServletRequest request) {
        return ResponseEntity.ok(apoService.update(id, apo, getCurrentUser(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        apoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // =========================
    // FLUXO
    // =========================

    @PostMapping("/{id}/aprovar-orientador")
    public ResponseEntity<APO> aprovarOrientador(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(apoService.aprovarOrientador(id, getCurrentUser(request)));
    }

    @PostMapping("/{id}/avaliar-orientador")
    public ResponseEntity<APO> avaliarOrientador(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean approved,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(apoService.avaliarOrientador(id, getCurrentUser(request), approved));
    }

    @PostMapping("/{id}/aprovar-coordenador")
    public ResponseEntity<APO> aprovarCoordenador(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(apoService.aprovarCoordenador(id, getCurrentUser(request)));
    }

    @PostMapping("/{id}/aprovar-comissao")
    public ResponseEntity<APO> aprovarComissao(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(apoService.aprovarComissao(id, getCurrentUser(request)));
    }

    @PostMapping("/{id}/rejeitar")
    public ResponseEntity<APO> rejeitar(@PathVariable Long id,
                                         @RequestBody(required = false) RejectionRequest body,
                                         HttpServletRequest request) {
        String justificativa = body != null ? body.getJustificativa() : null;
        if (justificativa == null || justificativa.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Justificativa é obrigatória para reprovação.");
        }
        return ResponseEntity.ok(apoService.rejeitar(id, getCurrentUser(request), justificativa));
    }

    @PostMapping(value = "/{id}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APO> uploadEvidencia(
            @PathVariable Long id,
            @RequestParam("arquivo") MultipartFile arquivo,
            HttpServletRequest request) {
        if (fileStorageService == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Serviço de arquivos indisponível.");
        }
        String hash = fileStorageService.calculateHash(arquivo);
        String path = fileStorageService.store(arquivo);
        FileItem fileItem = new FileItem(
                arquivo.getOriginalFilename(),
                path,
                hash,
                arquivo.getContentType(),
                arquivo.getSize()
        );
        return ResponseEntity.ok(apoService.addFile(id, fileItem, getCurrentUser(request)));
    }

    @GetMapping("/{id}/evidencias/{fileName:.+}")
    public ResponseEntity<byte[]> downloadEvidencia(
            @PathVariable Long id,
            @PathVariable String fileName,
            HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        APO apo = apoService.findById(id);
        if (!apoService.canAccessFile(apo, currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado ao arquivo.");
        }

        FileItem fileItem = apo.getFiles().stream()
                .filter(file -> fileName.equals(file.getUrl()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo não vinculado à APO."));

        byte[] bytes = fileStorageService.load(fileItem.getUrl());
        if (auditLogService != null) {
            auditLogService.registrar(currentUser, "DOWNLOAD", "APO", apo.getId(), "Download de evidência " + fileItem.getName());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileItem.getName() + "\"")
                .contentType(MediaType.parseMediaType(
                        fileItem.getContentType() != null ? fileItem.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(bytes);
    }

    private User getCurrentUser(HttpServletRequest request) {
        // Try to get from SecurityContext first
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

        // Fallback: try to parse token directly
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);
                if (username != null) {
                    return userService.findByUsername(username).orElse(null);
                }
            } catch (Exception e) {
                // Token parsing failed
            }
        }

        return null;
    }

    public static class RejectionRequest {
        private String justificativa;

        public String getJustificativa() {
            return justificativa;
        }

        public void setJustificativa(String justificativa) {
            this.justificativa = justificativa;
        }
    }
}
