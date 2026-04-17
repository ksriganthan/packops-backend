package ch.packops.packopsbackend.repository;

import ch.packops.packopsbackend.domain.Portion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PortionRepository extends JpaRepository<Portion, Long> {
    List<Portion> findByPackageUnitId(Long packageId);
}