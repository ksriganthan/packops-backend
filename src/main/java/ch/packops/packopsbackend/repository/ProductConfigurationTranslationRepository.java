package ch.packops.packopsbackend.repository;

import ch.packops.packopsbackend.domain.ProductConfiguration;
import ch.packops.packopsbackend.domain.ProductConfigurationTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

/**
 * @author Teodor Glisic
 */


public interface ProductConfigurationTranslationRepository extends JpaRepository<ProductConfigurationTranslation, Long> {
    ArrayList<ProductConfigurationTranslation> findAllByProductConfiguration(ProductConfiguration product);
}
