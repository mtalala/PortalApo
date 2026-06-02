package br.project.portalapo.service;

import br.project.portalapo.model.User;
import br.project.portalapo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setMustChangePassword(true);
        if (user.getOrientadorUserIds() == null) {
            user.setOrientadorUserIds(new ArrayList<>());
        }
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(userDetails.getUsername());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            user.setMustChangePassword(true);
        }
        user.setRole(userDetails.getRole());
        user.setAtivo(userDetails.isAtivo());
        if (userDetails.getOrientadorUserIds() != null) {
            user.setOrientadorUserIds(userDetails.getOrientadorUserIds());
        }
        return userRepository.save(user);
    }

    public User changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User forceChangePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        return userRepository.save(user);
    }

    public void sendVerificationCode(String email) {
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Gerar código aleatório de 6 dígitos
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // Armazenar código com expiração de 15 minutos
        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Enviar email
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("Código de Verificação - Portal Apoio");
                message.setText("Seu código de verificação é: " + code + "\n\nEste código expira em 15 minutos.");
                mailSender.send(message);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao enviar email: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Serviço de email não configurado");
        }
    }

    public boolean verifyCode(String email, String code) {
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (user.getVerificationCode() == null || user.getVerificationCodeExpiry() == null) {
            return false;
        }

        // Verificar se o código expirou
        if (LocalDateTime.now().isAfter(user.getVerificationCodeExpiry())) {
            user.setVerificationCode(null);
            user.setVerificationCodeExpiry(null);
            userRepository.save(user);
            return false;
        }

        // Verificar se o código está correto
        if (user.getVerificationCode().equals(code)) {
            user.setVerificationCode(null);
            user.setVerificationCodeExpiry(null);
            userRepository.save(user);
            return true;
        }

        return false;
    }
}
