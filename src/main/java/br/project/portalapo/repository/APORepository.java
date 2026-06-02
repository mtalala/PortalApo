package br.project.portalapo.repository;

import br.project.portalapo.enums.StatusAPO;
import br.project.portalapo.model.APO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<APO> findByAlunoUserIdAndStatus(Long alunoUserId, StatusAPO status);

    @Query("SELECT COUNT(a) > 0 FROM APO a JOIN a.files f WHERE a.alunoUserId = :alunoId AND a.semestre = :semestre AND f.hash = :hash")
    boolean existsDuplicateByHash(@Param("alunoId") Long alunoId,
                                  @Param("semestre") String semestre,
                                  @Param("hash") String hash);

    @Query("SELECT COALESCE(SUM(a.totalPoints), 0.0) FROM APO a WHERE a.alunoUserId = :alunoId AND a.status = 'APROVADA'")
    Double somarCreditosAprovados(@Param("alunoId") Long alunoId);
}
