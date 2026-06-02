package br.project.portalapo.controller;

import br.project.portalapo.config.JwtUtil;
import br.project.portalapo.model.APO;
import br.project.portalapo.model.User;
import br.project.portalapo.service.APOService;
import br.project.portalapo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apos")
public class APOController {

    private final APOService apoService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public APOController(APOService apoService, JwtUtil jwtUtil, UserService userService) {
        this.apoService = apoService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
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
                                       @RequestBody APO apo) {
        return ResponseEntity.ok(apoService.update(id, apo));
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
    public ResponseEntity<APO> rejeitar(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(apoService.rejeitar(id, getCurrentUser(request)));
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
}
