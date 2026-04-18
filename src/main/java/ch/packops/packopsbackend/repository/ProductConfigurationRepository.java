package ch.packops.packopsbackend.repository;

import ch.packops.packopsbackend.domain.ProductConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductConfigurationRepository extends JpaRepository<ProductConfiguration, Long> {
    List<ProductConfiguration> findByCategoryName(String categoryName);
}
