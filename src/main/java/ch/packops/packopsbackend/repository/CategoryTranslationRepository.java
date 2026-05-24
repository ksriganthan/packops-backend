package ch.packops.packopsbackend.repository;

import ch.packops.packopsbackend.domain.CategoryTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Teodor Glisic
 */

@Repository
public interface CategoryTranslationRepository extends JpaRepository<CategoryTranslation, Long> {
}
