package br.project.portalapo.controller;

import br.project.portalapo.model.Notification;
import br.project.portalapo.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Notification> getAll() {
        return service.findAll();
    }

    @GetMapping("/unread")
    public List<Notification> getUnread() {
        return service.findUnread();
    }

    @PatchMapping("/{id}/read")
    public void markAsRead(@PathVariable String id) {
        service.markAsRead(id);
    }

    @PatchMapping("/read-all")
    public void markAllAsRead() {
        service.markAllAsRead();
    }
}