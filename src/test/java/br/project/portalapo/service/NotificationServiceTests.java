package br.project.portalapo.service;

import br.project.portalapo.model.Notification;
import br.project.portalapo.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTests {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationService service;

    @Test
    void shouldReturnAllNotifications() {
        Notification n = new Notification();
        n.setId("1");
        n.setRole("Aluno");
        n.setTopic("Teste");

        when(repository.findAll()).thenReturn(List.of(n));

        List<Notification> result = service.findAll();

        assertEquals(1, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void shouldReturnUnreadNotifications() {
        Notification n1 = new Notification();
        n1.setId("1");
        n1.setRead(false);

        when(repository.findByReadFalse()).thenReturn(List.of(n1));

        List<Notification> result = service.findUnread();

        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
    }

    @Test
    void shouldMarkAsRead() {
        Notification n = new Notification();
        n.setId("1");
        n.setRead(false);

        when(repository.findById("1")).thenReturn(Optional.of(n));

        service.markAsRead("1");

        assertTrue(n.isRead());
    }

    @Test
    void shouldCreateNotification() {
        Notification saved = new Notification();
        saved.setId("1");
        saved.setRole("Aluno");

        when(repository.save(any(Notification.class))).thenReturn(saved);

        Notification result = service.create("Aluno", "Topic", "#fff");

        assertNotNull(result);
        verify(repository, times(1)).save(any(Notification.class));
    }
}