package br.project.portalapo.service;

import br.project.portalapo.model.PasswordResetToken;
import br.project.portalapo.model.User;
import br.project.portalapo.repository.PasswordResetTokenRepository;
import br.project.portalapo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:8081}")
    private String frontendUrl;

    @Transactional
    public void solicitarReset(String email) {
        // Busca por username (que pode ser o email no sistema)
        User user = userRepository.findByUsername(email)
                .orElse(null);

        // Se não encontrar por username, tenta por email
        if (user == null) {
            user = userRepository.findByEmail(email).orElse(null);
        }

        // Sempre retorna sem revelar se o usuário existe
        if (user == null) {
            return;
        }

        // Invalida tokens anteriores do usuário
        tokenRepository.deleteByUsername(user.getUsername());

        // Cria novo token
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(tokenValue, user.getUsername());
        tokenRepository.save(resetToken);

        // Envia email
        if (mailSender != null) {
            try {
                String link = frontendUrl + "/redefinir-senha?token=" + tokenValue;
                SimpleMailMessage message = new SimpleMailMessage();
                String destEmail = (user.getEmail() != null && !user.getEmail().isBlank())
                        ? user.getEmail() : email;
                message.setTo(destEmail);
                message.setSubject("Portal APO — Redefinição de senha");
                message.setText(
                        "Olá,\n\n" +
                        "Recebemos uma solicitação de redefinição de senha para sua conta no Portal APO.\n\n" +
                        "Identificador da solicitação: " + tokenValue + "\n\n" +
                        "Clique no link abaixo para redefinir sua senha (válido por 1 hora):\n" +
                        link + "\n\n" +
                        "Se você não solicitou a redefinição, ignore este email.\n\n" +
                        "Equipe Portal APO"
                );
                mailSender.send(message);
            } catch (Exception e) {
                // Log silencioso — não revelar falha ao cliente
            }
        }
    }

    @Transactional
    public void redefinirSenha(String tokenValue, String novaSenha) {
        PasswordResetToken resetToken = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Token inválido ou expirado."));

        if (resetToken.isUsado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token já utilizado.");
        }

        if (LocalDateTime.now().isAfter(resetToken.getExpiraEm())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expirado.");
        }

        User user = userRepository.findByUsername(resetToken.getUsername())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        user.setPassword(passwordEncoder.encode(novaSenha));
        user.setMustChangePassword(false);
        userRepository.save(user);

        resetToken.setUsado(true);
        tokenRepository.save(resetToken);
    }
}
