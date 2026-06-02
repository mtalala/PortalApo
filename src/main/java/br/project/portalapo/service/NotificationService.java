package br.project.portalapo.service;

import br.project.portalapo.model.Notification;
import br.project.portalapo.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public List<Notification> findAll() {
        return repository.findAll();
    }

    public List<Notification> findUnread() {
        return repository.findByReadFalse();
    }

    @Transactional
    public void markAsRead(String id) {
        repository.findById(id)
                .ifPresent(n -> n.setRead(true));
    }

    @Transactional
    public void markAllAsRead() {
        repository.findAll()
                .forEach(n -> n.setRead(true));
    }

    public Notification create(
            String role,
            String topic,
            String requestColor
    ) {
        return repository.save(
                new Notification(role, topic, requestColor)
        );
    }
}