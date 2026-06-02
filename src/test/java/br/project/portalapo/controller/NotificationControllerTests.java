package br.project.portalapo.controller;

import br.project.portalapo.model.Notification;
import br.project.portalapo.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAllNotifications() throws Exception {
        Notification n = new Notification();
        n.setId("1");
        n.setRole("Aluno");
        n.setTopic("Teste");

        when(service.findAll()).thenReturn(List.of(n));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].role").value("Aluno"));
    }

    @Test
    void shouldReturnUnreadNotifications() throws Exception {
        Notification n = new Notification();
        n.setId("1");
        n.setRead(false);

        when(service.findUnread()).thenReturn(List.of(n));

        mockMvc.perform(get("/api/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    void shouldMarkAsRead() throws Exception {
        doNothing().when(service).markAsRead("1");

        mockMvc.perform(patch("/api/notifications/1/read"))
                .andExpect(status().isOk());

        verify(service, times(1)).markAsRead("1");
    }

    @Test
    void shouldMarkAllAsRead() throws Exception {
        doNothing().when(service).markAllAsRead();

        mockMvc.perform(patch("/api/notifications/read-all"))
                .andExpect(status().isOk());

        verify(service, times(1)).markAllAsRead();
    }
}