package br.project.portalapo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Aluno Model Tests")
class AlunoModelTests {

    private Aluno aluno;

    @BeforeEach
    void setUp() {
        aluno = new Aluno();
    }

    @Test
    @DisplayName("Should create aluno with all properties")
    void testAlunoCreation() {
        aluno.setId(1L);
        aluno.setNome("João da Silva");
        aluno.setEmail("joao@university.edu");
        aluno.setRa("2023001");
        aluno.setCurso("Engenharia");
        aluno.setPeriodo(4);

        assertEquals(1L, aluno.getId());
        assertEquals("João da Silva", aluno.getNome());
        assertEquals("joao@university.edu", aluno.getEmail());
        assertEquals("2023001", aluno.getRa());
        assertEquals("Engenharia", aluno.getCurso());
        assertEquals(4, aluno.getPeriodo());
    }

    @Test
    @DisplayName("Should set and get nome")
    void testSetAndGetNome() {
        aluno.setNome("Maria Santos");
        assertEquals("Maria Santos", aluno.getNome());
    }

    @Test
    @DisplayName("Should set and get ra")
    void testSetAndGetRa() {
        aluno.setRa("2024001");
        assertEquals("2024001", aluno.getRa());
    }

    @Test
    @DisplayName("Should set and get curso")
    void testSetAndGetCurso() {
        aluno.setCurso("Computação");
        assertEquals("Computação", aluno.getCurso());
    }

    @Test
    @DisplayName("Should set and get periodo")
    void testSetAndGetPeriodo() {
        aluno.setPeriodo(6);
        assertEquals(6, aluno.getPeriodo());
    }

    @Test
    @DisplayName("Should use NoArgsConstructor")
    void testNoArgsConstructor() {
        Aluno newAluno = new Aluno();
        assertNotNull(newAluno);
        assertNull(newAluno.getId());
    }

    @Test
    @DisplayName("Should use AllArgsConstructor (Aluno fields only)")
    void testAllArgsConstructor() {
        Aluno newAluno = new Aluno(null, null, null, false, "2023002", "Arquitetura", 3);

        // campos da classe Aluno
        assertEquals("2023002", newAluno.getRa());
        assertEquals("Arquitetura", newAluno.getCurso());
        assertEquals(3, newAluno.getPeriodo());

        // campos herdados (Pessoa) permanecem nulos até set manual
        assertNull(newAluno.getId());
        assertNull(newAluno.getNome());
        assertNull(newAluno.getEmail());
    }
}