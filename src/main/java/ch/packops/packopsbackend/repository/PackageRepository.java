package ch.packops.packopsbackend.repository;

import ch.packops.packopsbackend.domain.PackageUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<PackageUnit, Long> {
    List<PackageUnit> findByProcessId(Long processId);
    List<PackageUnit> findByProcessProductConfigurationId(Long productConfigurationId);
    List<PackageUnit> findByProcessProductConfigurationIsNull();
}
