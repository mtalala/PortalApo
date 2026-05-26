package br.project.portalapo.repository;

import br.project.portalapo.model.APO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface APORepository extends JpaRepository<APO, Long> {
    List<APO> findByAlunoUserId(Long alunoUserId);
}
