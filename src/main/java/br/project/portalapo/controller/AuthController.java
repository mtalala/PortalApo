package br.project.portalapo.controller;

import br.project.portalapo.config.JwtUtil;
import br.project.portalapo.model.User;
import br.project.portalapo.service.PasswordResetService;
import br.project.portalapo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;
    private final ConcurrentHashMap<String, Integer> failedLoginAttempts = new ConcurrentHashMap<>();

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder,
                          PasswordResetService passwordResetService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body("Usuário é obrigatório");
        }

        if (loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Senha é obrigatória");
        }

        String username = loginRequest.getUsername();
        if (failedLoginAttempts.getOrDefault(username, 0) >= 5) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Conta bloqueada temporariamente. Use a recuperação de senha.");
        }

        User user = userService.findByUsername(username).orElse(null);
        if (user == null) {
            int attempts = failedLoginAttempts.merge(username, 1, Integer::sum);
            if (attempts >= 5) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Conta bloqueada temporariamente. Use a recuperação de senha.");
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário ou senha inválidos");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            int attempts = failedLoginAttempts.merge(username, 1, Integer::sum);
            if (attempts >= 5) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Conta bloqueada temporariamente. Use a recuperação de senha.");
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário ou senha inválidos");
        }

        failedLoginAttempts.remove(username);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("token", token);
        response.put("role", user.getRole());
        response.put("mustChangePassword", user.isMustChangePassword());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest,
                                            HttpServletRequest request) {
        String username = getCurrentUsername(request);

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
        }

        if (changePasswordRequest == null || changePasswordRequest.getCurrentPassword() == null || changePasswordRequest.getCurrentPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Senha atual é obrigatória");
        }

        if (changePasswordRequest.getNewPassword() == null || changePasswordRequest.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Nova senha é obrigatória");
        }

        if (changePasswordRequest.getNewPassword().equals(changePasswordRequest.getCurrentPassword())) {
            return ResponseEntity.badRequest().body("A nova senha deve ser diferente da senha atual");
        }

        try {
            User user = userService.changePassword(
                    username,
                    changePasswordRequest.getCurrentPassword(),
                    changePasswordRequest.getNewPassword()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("mustChangePassword", user.isMustChangePassword());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha atual inválida");
        }
    }

    @PostMapping("/force-change-password")
    public ResponseEntity<?> forceChangePassword(@RequestBody ForceChangePasswordRequest forceChangePasswordRequest,
                                                 HttpServletRequest request) {
        String username = getCurrentUsername(request);

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
        }

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!user.isMustChangePassword()) {
            return ResponseEntity.badRequest().body("Alteração de senha não é obrigatória para este usuário");
        }

        if (forceChangePasswordRequest.getNewPassword() == null || forceChangePasswordRequest.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Nova senha é obrigatória");
        }

        try {
            User updatedUser = userService.forceChangePassword(username, forceChangePasswordRequest.getNewPassword());

            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("username", updatedUser.getUsername());
            response.put("role", updatedUser.getRole());
            response.put("mustChangePassword", updatedUser.isMustChangePassword());
            response.put("message", "Senha alterada com sucesso");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao alterar senha");
        }
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody SendVerificationCodeRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email é obrigatório");
        }

        try {
            userService.sendVerificationCode(request.getEmail());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Código de verificação enviado para o email");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao enviar código de verificação");
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email é obrigatório");
        }

        if (request.getCode() == null || request.getCode().isBlank()) {
            return ResponseEntity.badRequest().body("Código é obrigatório");
        }

        try {
            boolean isValid = userService.verifyCode(request.getEmail(), request.getCode());
            
            if (isValid) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Código verificado com sucesso");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body("Código de verificação inválido ou expirado");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao verificar código");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        if (req == null || req.getEmail() == null || req.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email é obrigatório");
        }
        // Sempre retorna 200 — não revelar se o e-mail existe ou não
        passwordResetService.solicitarReset(req.getEmail());
        return ResponseEntity.ok(java.util.Map.of(
                "message", "Se o e-mail estiver cadastrado, você receberá um link para redefinir sua senha."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        if (req == null || req.getToken() == null || req.getToken().isBlank()) {
            return ResponseEntity.badRequest().body("Token é obrigatório");
        }
        if (req.getNovaSenha() == null || req.getNovaSenha().isBlank()) {
            return ResponseEntity.badRequest().body("Nova senha é obrigatória");
        }
        if (req.getNovaSenha().length() < 6) {
            return ResponseEntity.badRequest().body("A nova senha deve ter pelo menos 6 caracteres");
        }
        passwordResetService.redefinirSenha(req.getToken(), req.getNovaSenha());
        failedLoginAttempts.clear();
        return ResponseEntity.ok(java.util.Map.of("message", "Senha redefinida com sucesso."));
    }

    private String getCurrentUsername(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            }

            if (principal instanceof String name) {
                return name;
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                return jwtUtil.extractUsername(authHeader.substring(7));
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    public static class LoginRequest {

        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class ChangePasswordRequest {

        private String currentPassword;
        private String newPassword;

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    public static class ForceChangePasswordRequest {

        private String newPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    public static class SendVerificationCodeRequest {

        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class VerifyCodeRequest {

        private String email;
        private String code;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public static class ForgotPasswordRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ResetPasswordRequest {
        private String token;
        private String novaSenha;
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getNovaSenha() { return novaSenha; }
        public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
    }
}
