package br.project.portalapo.repository;

import br.project.portalapo.enums.StatusAPO;
import br.project.portalapo.model.APO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface APORepository extends JpaRepository<APO, Long> {
    List<APO> findByAlunoUserId(Long alunoUserId);
    List<APO> findByOrientadorUserIdsContaining(Long orientadorUserId);
    List<APO> findByCoordenadorUserIdAndStatusNot(Long coordenadorUserId, StatusAPO status);
    List<APO> findByCoordenadorUsernameAndStatusNot(String coordenadorUsername, StatusAPO status);
    List<APO> findByCoordenadorUserId(Long coordenadorUserId);
    List<APO> findByCoordenadorUsername(String coordenadorUsername);
    List<APO> findByStatus(br.project.portalapo.enums.StatusAPO status);
}
