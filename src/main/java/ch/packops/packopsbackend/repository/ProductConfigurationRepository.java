package ch.packops.packopsbackend.repository;

import ch.packops.packopsbackend.domain.ProductConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductConfigurationRepository extends JpaRepository<ProductConfiguration, Long> {

}
