package br.project.portalapo.config;

import br.project.portalapo.model.User;
import br.project.portalapo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        System.out.println("INIT RUNNING");

        User admin = userRepository.findByUsername("admin")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername("admin");
                    u.setPassword(passwordEncoder.encode("admin123"));
                    u.setRole("ADMIN");
                    u.setAtivo(true);
                    return userRepository.save(u);
                });

        System.out.println("USER: " + admin.getUsername());
    }
}