package ch.packops.packopsbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ch.packops.packopsbackend.domain.Process;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProcessRepository extends JpaRepository<Process, Long> {
    List<Process> findByUserId(Long userId);
    List<Process> findByProductConfigurationId(Long productConfigurationId);
    List<Process> findByProductConfigurationIsNull();
}
